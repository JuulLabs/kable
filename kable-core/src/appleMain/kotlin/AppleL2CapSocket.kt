@file:OptIn(ExperimentalForeignApi::class, NativeRuntimeApi::class)

package com.juul.kable

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import platform.CoreBluetooth.CBL2CAPChannel
import platform.CoreFoundation.CFRunLoopRef
import platform.CoreFoundation.CFRunLoopWakeUp
import platform.Foundation.NSDate
import platform.Foundation.NSDefaultRunLoopMode
import platform.Foundation.NSError
import platform.Foundation.NSInputStream
import platform.Foundation.NSLock
import platform.Foundation.NSOutputStream
import platform.Foundation.NSRunLoop
import platform.Foundation.NSStream
import platform.Foundation.NSStreamDelegateProtocol
import platform.Foundation.NSStreamEvent
import platform.Foundation.NSStreamEventEndEncountered
import platform.Foundation.NSStreamEventErrorOccurred
import platform.Foundation.NSStreamEventHasBytesAvailable
import platform.Foundation.NSStreamEventHasSpaceAvailable
import platform.Foundation.NSThread
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runMode
import platform.darwin.NSObject
import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi

// CBL2CAPChannel's NSStreams are not thread-safe: driving one from a rotating dispatcher thread pool
// can make an output-stream write intermittently fail with EINVAL when a write lands on a different
// thread than earlier stream operations. This socket therefore confines *every* stream operation to
// one dedicated thread with a running NSRunLoop, the model Apple's CBL2CAPChannel sample uses:
//   - Reads are event-driven. The NSStreamDelegate reads on HasBytesAvailable into an unbounded
//     channel; read() only drains that channel, so it never touches the stream itself and a blocking
//     read can never starve a write. The channel is unbounded so a slow reader applies backpressure by
//     buffering rather than dropping inbound data.
//   - Writes are queued from the caller's coroutine and pumped by the run-loop thread, so the write
//     syscall only ever runs on the run-loop thread. Writability comes from the polled
//     hasSpaceAvailable property when work is queued, with the HasSpaceAvailable event as the resume
//     signal after backpressure — the event alone is an edge NSStream is not obliged to (re-)emit
//     before the first write, so gating on it alone would deadlock the first outbound packet.
// EOF/error sign conventions are the inverse of L2CapSocket's (NSInputStream: 0 = EOF, -1 = error),
// normalised where the channel is closed. A read error closes the channel with a cause (drained, then
// read() rethrows); EOF closes it without one (drained, then read() returns -1).
private const val READ_CHUNK_SIZE = 8192

// The run loop wakes on stream events for reads; this bounds how long a queued *write* waits when the
// loop is otherwise idle. CFRunLoopWakeUp normally makes writes immediate — this is the safety net if a
// wake is missed, so a queued write is never delayed by more than this interval.
private const val RUN_LOOP_POLL_SECONDS = 0.1

internal class AppleL2CapSocket(
    channel: CBL2CAPChannel,
) : L2CapSocket {

    // Retained while the socket is in use: CoreBluetooth closes the underlying channel when the
    // CBL2CAPChannel deallocates, and the streams are not documented to keep it alive. Unretained, the
    // channel can be collected while still in use, dropping the link shortly after the first data burst.
    // Nulled at teardown for the same reason in reverse: Kotlin/Native releases an Obj-C reference only
    // when the GC collects it, and deallocation is also the only way CoreBluetooth *disconnects* the
    // channel (closing the streams does not); a socket held by its caller must not keep the PSM
    // connected, or reopening it fails with "L2CAP PSM already connected" until an incidental GC runs.
    private var retainedChannel: CBL2CAPChannel? = channel

    private val inputStream: NSInputStream = channel.inputStream ?: error("L2CAP channel has no input stream")
    private val outputStream: NSOutputStream = channel.outputStream ?: error("L2CAP channel has no output stream")

    // An NSStream delegate must be an Obj-C class, but L2CapSocket is a Kotlin interface and K/N forbids
    // mixing the two supertypes on one class, so the delegate is its own object that forwards events here.
    private val streamDelegate = StreamDelegate()

    private val _isConnected = MutableStateFlow(true)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _hasReachedEof = MutableStateFlow(false)
    override val hasReachedEof: StateFlow<Boolean> = _hasReachedEof.asStateFlow()

    // Chunks read on the run-loop thread, drained by the single reader coroutine in read().
    private val incoming = Channel<ByteArray>(Channel.UNLIMITED)

    private class PendingWrite(val packet: ByteArray, val done: CompletableDeferred<Unit>)

    // writeQueue and the two flags below are the only state shared with caller threads; NSLock guards
    // them. Everything else (current/currentOffset/spaceAvailable/pendingWrites/terminal) is touched
    // only on the run-loop thread.
    private val lock = NSLock()
    private val writeQueue = ArrayDeque<PendingWrite>()
    private var stopRequested = false
    private var acceptingWrites = true
    private var cfRunLoop: CFRunLoopRef? = null

    private val pendingWrites = ArrayDeque<PendingWrite>()
    private var current: PendingWrite? = null
    private var currentOffset = 0
    private var spaceAvailable = false
    private var terminal: Throwable? = null
    private var streamsClosed = false

    // Completed by the run-loop thread once the streams are closed and unscheduled, so close() can
    // guarantee teardown finished rather than merely being requested.
    private val closed = CompletableDeferred<Unit>()

    private fun runLoop() {
        try {
            runLoopBody()
        } finally {
            // Unconditional so close() can never hang on a run-loop thread that died unexpectedly —
            // in that case the streams may be left un-unscheduled, but the thread is gone and no
            // further cleanup is possible.
            closed.complete(Unit)
        }
    }

    private fun runLoopBody() {
        val runLoop = NSRunLoop.currentRunLoop
        lock.lock()
        cfRunLoop = runLoop.getCFRunLoop()
        lock.unlock()

        streamDelegate.onEvent = ::handleStreamEvent
        inputStream.delegate = streamDelegate
        outputStream.delegate = streamDelegate
        inputStream.scheduleInRunLoop(runLoop, forMode = NSDefaultRunLoopMode)
        outputStream.scheduleInRunLoop(runLoop, forMode = NSDefaultRunLoopMode)
        inputStream.open()
        outputStream.open()

        while (true) {
            if (processControlAndWrites()) break
            runLoop.runMode(NSDefaultRunLoopMode, beforeDate = NSDate.dateWithTimeIntervalSinceNow(RUN_LOOP_POLL_SECONDS))
            if (terminal != null) break
        }

        beginClose()
        cleanup()
        // Null the handle before the run loop is torn down with this thread, so a close()/write() racing
        // in after the loop exits skips CFRunLoopWakeUp rather than waking a dangling run loop. wakeRunLoop
        // reads and wakes under the same lock, so a wake can only fire while this section hasn't run yet.
        lock.lock()
        cfRunLoop = null
        retainedChannel = null
        lock.unlock()
        // The channel disconnects on deallocation (see retainedChannel); ask for a collection so an
        // abandoned socket — whose owner never gets to call close() — frees its PSM promptly too.
        GC.schedule()
    }

    // Run-loop thread. Moves caller-enqueued writes into the run-loop-local queue and pumps them.
    // Returns true when the loop should exit (graceful close requested, or the socket went terminal).
    private fun processControlAndWrites(): Boolean {
        if (terminal != null) return true
        lock.lock()
        val stop = stopRequested
        val staged = if (writeQueue.isEmpty()) emptyList() else writeQueue.toList().also { writeQueue.clear() }
        lock.unlock()
        if (stop) return true
        if (staged.isNotEmpty()) pendingWrites.addAll(staged)
        // HasSpaceAvailable is an edge signal: it may have fired before any write was queued (and is
        // then spent) or not at all yet. With work pending, seed the flag from the polled property so
        // a write never waits for an event that already passed; this re-runs every loop iteration, so
        // even without the event a queued write proceeds within RUN_LOOP_POLL_SECONDS.
        if ((current != null || pendingWrites.isNotEmpty()) && !spaceAvailable && outputStream.hasSpaceAvailable) {
            spaceAvailable = true
        }
        pump()
        return terminal != null
    }

    // Run-loop thread, via streamDelegate. Events can OR together, so each bit gets its own `if` —
    // a `when` would handle only the first matching bit and silently drop the rest.
    private fun handleStreamEvent(aStream: NSStream, event: NSStreamEvent) {
        // Bit tests are written as `(event and bit) == bit` rather than `!= 0uL` so they hold on every
        // Apple target: NSStreamEvent is a 32-bit UInt on watchosArm64 (arm64_32) and 64-bit elsewhere,
        // and a fixed-width `0uL` literal would not compile against the narrow variant.
        if (event and NSStreamEventHasBytesAvailable == NSStreamEventHasBytesAvailable) onBytesAvailable()
        if (event and NSStreamEventHasSpaceAvailable == NSStreamEventHasSpaceAvailable) {
            spaceAvailable = true
            pump()
        }
        if (event and NSStreamEventErrorOccurred == NSStreamEventErrorOccurred) onError(aStream.streamError)
        if (event and NSStreamEventEndEncountered == NSStreamEventEndEncountered) onEnd()
    }

    private fun onBytesAvailable() {
        while (terminal == null && inputStream.hasBytesAvailable) {
            val scratch = ByteArray(READ_CHUNK_SIZE)
            // `.convert()` for the length and `.toLong()` on the result: NSInputStream.read's length is
            // an NSUInteger and its return an NSInteger, both of which narrow to 32-bit on watchosArm64.
            val read = scratch.usePinned { pinned ->
                inputStream.read(pinned.addressOf(0).reinterpret(), READ_CHUNK_SIZE.convert())
            }.toLong()
            when {
                read > 0L -> incoming.trySend(scratch.copyOf(read.toInt()))
                read == 0L -> {
                    onEnd()
                    return
                }
                else -> {
                    onError(inputStream.streamError)
                    return
                }
            }
        }
    }

    private fun pump() {
        while (terminal == null && spaceAvailable) {
            val write = current ?: pendingWrites.removeFirstOrNull()?.also {
                current = it
                currentOffset = 0
            } ?: break
            val packet = write.packet
            val remaining = packet.size - currentOffset
            val written = packet.usePinned { pinned ->
                outputStream.write(pinned.addressOf(currentOffset).reinterpret(), remaining.convert())
            }.toLong()
            when {
                written > 0L -> {
                    currentOffset += written.toInt()
                    if (currentOffset >= packet.size) {
                        write.done.complete(Unit)
                        current = null
                        currentOffset = 0
                    }
                    if (!outputStream.hasSpaceAvailable) spaceAvailable = false
                }
                written == 0L -> spaceAvailable = false
                else -> {
                    onError(outputStream.streamError)
                    return
                }
            }
        }
    }

    // EOF: reader drains buffered chunks then read() returns -1. Writes can no longer complete, so fail
    // any in flight. Idempotent — End and a 0-length read can both arrive.
    private fun onEnd() {
        if (terminal != null) return
        // EOF is not an error for reads (the channel closes without a cause so read() returns -1), but
        // it still ends the run loop and must fail any in-flight write against the now-dead channel.
        terminal = L2CapException("L2CAP stream reached end of file", code = 0L)
        _hasReachedEof.value = true
        _isConnected.value = false
        incoming.close()
        failAllWrites(terminal!!)
    }

    private fun onError(error: NSError?) {
        if (terminal != null) return
        val exception = L2CapException(error?.localizedDescription ?: "L2CAP stream error", code = 0L)
        terminal = exception
        _isConnected.value = false
        incoming.close(exception)
        failAllWrites(exception)
    }

    private fun failAllWrites(cause: Throwable) {
        current?.done?.completeExceptionally(cause)
        current = null
        currentOffset = 0
        pendingWrites.forEach { it.done.completeExceptionally(cause) }
        pendingWrites.clear()
        lock.lock()
        acceptingWrites = false
        val queued = if (writeQueue.isEmpty()) emptyList() else writeQueue.toList().also { writeQueue.clear() }
        lock.unlock()
        queued.forEach { it.done.completeExceptionally(cause) }
    }

    // Run-loop thread: unschedule and close the streams on the thread they live on. Idempotent.
    private fun beginClose() {
        if (streamsClosed) return
        streamsClosed = true
        val runLoop = NSRunLoop.currentRunLoop
        inputStream.delegate = null
        outputStream.delegate = null
        // The socket owns the delegate, so it outlives the streams: clearing their reference to it is
        // not enough to stop an event already in flight re-entering a socket that has closed.
        streamDelegate.onEvent = null
        inputStream.removeFromRunLoop(runLoop, forMode = NSDefaultRunLoopMode)
        outputStream.removeFromRunLoop(runLoop, forMode = NSDefaultRunLoopMode)
        inputStream.close()
        outputStream.close()
    }

    private fun cleanup() {
        val cause = terminal ?: L2CapException("L2CAP socket closed", code = 0L)
        failAllWrites(cause)
        if (!incoming.isClosedForSend) incoming.close()
    }

    // Reader-owned and unguarded: read() assumes the single reader coroutine its contract promises.
    private var leftover: ByteArray? = null
    private var leftoverOffset = 0

    override suspend fun read(buffer: ByteArray): Int {
        if (buffer.isEmpty()) return 0
        leftover?.let { chunk ->
            val count = minOf(buffer.size, chunk.size - leftoverOffset)
            chunk.copyInto(buffer, destinationOffset = 0, startIndex = leftoverOffset, endIndex = leftoverOffset + count)
            leftoverOffset += count
            if (leftoverOffset >= chunk.size) {
                leftover = null
                leftoverOffset = 0
            }
            return count
        }
        val result = incoming.receiveCatching()
        if (result.isClosed) {
            // Cause is non-null only for a read error; a clean EOF closes the channel without one.
            result.exceptionOrNull()?.let { throw it }
            return -1
        }
        val chunk = result.getOrThrow()
        val count = minOf(buffer.size, chunk.size)
        chunk.copyInto(buffer, destinationOffset = 0, startIndex = 0, endIndex = count)
        if (count < chunk.size) {
            leftover = chunk
            leftoverOffset = count
        }
        return count
    }

    override suspend fun write(packet: ByteArray) {
        if (packet.isEmpty()) return
        val done = CompletableDeferred<Unit>()
        val pending = PendingWrite(packet, done)
        lock.lock()
        val accepted = acceptingWrites
        if (accepted) writeQueue.addLast(pending)
        lock.unlock()
        if (!accepted) throw L2CapException("L2CAP socket is closed", code = 0L)
        wakeRunLoop()
        try {
            done.await()
        } catch (e: CancellationException) {
            // The caller abandoned this write, so drop it if the run loop has not staged it yet
            // rather than delivering a packet whose sender is already gone. One that is staged (or
            // half-written) is past recall and still completes.
            lock.lock()
            writeQueue.remove(pending)
            lock.unlock()
            throw e
        }
    }

    // Teardown for a socket whose owner never materialised: openL2CapChannel was cancelled while
    // CoreBluetooth was still opening the channel, and the channel then arrived anyway. Unlike close()
    // this does not await the run loop, so the delegate can call it from its non-suspending callback;
    // the run loop's finally still completes `closed`.
    internal fun abandon() {
        _isConnected.value = false
        lock.lock()
        stopRequested = true
        acceptingWrites = false
        val queued = if (writeQueue.isEmpty()) emptyList() else writeQueue.toList().also { writeQueue.clear() }
        lock.unlock()
        queued.forEach { it.done.completeExceptionally(L2CapException("L2CAP socket is closed", code = 0L)) }
        wakeRunLoop()
    }

    override suspend fun close() {
        _isConnected.value = false
        lock.lock()
        stopRequested = true
        acceptingWrites = false
        val queued = if (writeQueue.isEmpty()) emptyList() else writeQueue.toList().also { writeQueue.clear() }
        lock.unlock()
        queued.forEach { it.done.completeExceptionally(L2CapException("L2CAP socket is closed", code = 0L)) }
        wakeRunLoop()
        // Close must not report done before it is: streams left live here are stale local L2CAP state
        // that can poison a subsequent channel opened on the same PSM, so await full teardown — shielded
        // from cancellation, which must not be able to skip the collection below.
        withContext(NonCancellable) {
            closed.await()
            // Deterministic disconnect: the OS only tears the channel down when the CBL2CAPChannel
            // deallocates (see retainedChannel), and Kotlin/Native releases it at collection time.
            // Without this, close() returns with the PSM still connected and a reopen fails with
            // "L2CAP PSM already connected" until an incidental GC happens to run.
            GC.collect()
        }
    }

    private fun wakeRunLoop() {
        // Wake under the lock: while it is held, the run-loop thread cannot reach its final section that
        // nulls cfRunLoop and lets the run loop deallocate, so a non-null handle here is alive to wake.
        lock.lock()
        cfRunLoop?.let { CFRunLoopWakeUp(it) }
        lock.unlock()
    }

    // Last: every field above is initialized before the run-loop thread (which touches them) starts.
    init {
        // Connected up front: a consumer may gate on isConnected before its first read, and read() then
        // suspends on `incoming` until the streams open and data arrives. Setting this on the run-loop
        // thread instead would race that gate closed.
        _isConnected.value = true
        val thread = NSThread { runLoop() }
        thread.name = "l2cap-runloop"
        thread.start()
    }
}

// NSStream delivers events to a delegate scheduled on the run loop. It forwards to the socket via a
// callback so AppleL2CapSocket can stay a plain Kotlin L2CapSocket (K/N forbids one class extending both
// an Obj-C type and a Kotlin interface). NSStream holds its delegate weakly, so the socket owns this.
private class StreamDelegate : NSObject(), NSStreamDelegateProtocol {

    var onEvent: ((NSStream, NSStreamEvent) -> Unit)? = null

    override fun stream(aStream: NSStream, handleEvent: NSStreamEvent) {
        onEvent?.invoke(aStream, handleEvent)
    }
}
