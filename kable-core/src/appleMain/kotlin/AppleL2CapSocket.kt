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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

private const val READ_CHUNK_SIZE = 8192
private const val RUN_LOOP_POLL_SECONDS = 0.1

// The channel's streams are not thread safe, so all stream access happens on one dedicated thread
// running an NSRunLoop.
internal class AppleL2CapSocket(
    channel: CBL2CAPChannel,
) : L2CapSocket {

    // CoreBluetooth disconnects the channel when the CBL2CAPChannel is deallocated, and not before.
    private var retainedChannel: CBL2CAPChannel? = channel

    private val inputStream: NSInputStream = channel.inputStream ?: error("L2CAP channel has no input stream")
    private val outputStream: NSOutputStream = channel.outputStream ?: error("L2CAP channel has no output stream")
    private val streamDelegate = StreamDelegate()

    private val _isConnected = MutableStateFlow(true)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val chunks = Channel<ByteArray>(Channel.UNLIMITED)
    override val incoming: Flow<ByteArray> = chunks.receiveAsFlow()

    private class PendingWrite(val packet: ByteArray, val done: CompletableDeferred<Unit>)

    // Guarded by lock, shared with caller threads.
    private val lock = NSLock()
    private val writeQueue = ArrayDeque<PendingWrite>()
    private var stopRequested = false
    private var acceptingWrites = true
    private var cfRunLoop: CFRunLoopRef? = null

    // Run loop thread only.
    private val pendingWrites = ArrayDeque<PendingWrite>()
    private var current: PendingWrite? = null
    private var currentOffset = 0
    private var spaceAvailable = false
    private var terminal: Throwable? = null
    private var streamsClosed = false

    private val closed = CompletableDeferred<Unit>()

    private fun runLoop() {
        try {
            runLoopBody()
        } finally {
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
        lock.lock()
        cfRunLoop = null
        retainedChannel = null
        lock.unlock()
        GC.schedule()
    }

    private fun processControlAndWrites(): Boolean {
        if (terminal != null) return true
        lock.lock()
        val stop = stopRequested
        val staged = if (writeQueue.isEmpty()) emptyList() else writeQueue.toList().also { writeQueue.clear() }
        lock.unlock()
        if (stop) return true
        if (staged.isNotEmpty()) pendingWrites.addAll(staged)
        // HasSpaceAvailable may have fired before anything was queued, so also poll the property.
        if ((current != null || pendingWrites.isNotEmpty()) && !spaceAvailable && outputStream.hasSpaceAvailable) {
            spaceAvailable = true
        }
        pump()
        return terminal != null
    }

    private fun handleStreamEvent(aStream: NSStream, event: NSStreamEvent) {
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
            val read = scratch.usePinned { pinned ->
                inputStream.read(pinned.addressOf(0).reinterpret(), READ_CHUNK_SIZE.convert())
            }.toLong()
            when {
                read > 0L -> chunks.trySend(scratch.copyOf(read.toInt()))
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

    private fun onEnd() {
        if (terminal != null) return
        terminal = L2CapException("L2CAP stream reached end of file")
        _isConnected.value = false
        chunks.close()
        failAllWrites(terminal!!)
    }

    private fun onError(error: NSError?) {
        if (terminal != null) return
        val exception = L2CapException(error?.localizedDescription ?: "L2CAP stream error", code = error?.code?.toLong())
        terminal = exception
        _isConnected.value = false
        chunks.close(exception)
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

    private fun beginClose() {
        if (streamsClosed) return
        streamsClosed = true
        val runLoop = NSRunLoop.currentRunLoop
        inputStream.delegate = null
        outputStream.delegate = null
        streamDelegate.onEvent = null
        inputStream.removeFromRunLoop(runLoop, forMode = NSDefaultRunLoopMode)
        outputStream.removeFromRunLoop(runLoop, forMode = NSDefaultRunLoopMode)
        inputStream.close()
        outputStream.close()
    }

    private fun cleanup() {
        val cause = terminal ?: L2CapException("L2CAP socket closed")
        failAllWrites(cause)
        if (!chunks.isClosedForSend) chunks.close()
    }

    override suspend fun write(packet: ByteArray) {
        require(packet.isNotEmpty()) { "Packet must not be empty" }
        val done = CompletableDeferred<Unit>()
        val pending = PendingWrite(packet, done)
        lock.lock()
        val accepted = acceptingWrites
        if (accepted) writeQueue.addLast(pending)
        lock.unlock()
        if (!accepted) throw L2CapException("L2CAP socket is closed")
        wakeRunLoop()
        try {
            done.await()
        } catch (e: CancellationException) {
            lock.lock()
            writeQueue.remove(pending)
            lock.unlock()
            throw e
        }
    }

    internal fun dispose() {
        _isConnected.value = false
        lock.lock()
        stopRequested = true
        acceptingWrites = false
        val queued = if (writeQueue.isEmpty()) emptyList() else writeQueue.toList().also { writeQueue.clear() }
        lock.unlock()
        queued.forEach { it.done.completeExceptionally(L2CapException("L2CAP socket is closed")) }
        wakeRunLoop()
    }

    override suspend fun close() {
        dispose()
        withContext(NonCancellable) {
            closed.await()
            // Releases the CBL2CAPChannel now, so the PSM can be reopened right away.
            GC.collect()
        }
    }

    private fun wakeRunLoop() {
        lock.lock()
        cfRunLoop?.let { CFRunLoopWakeUp(it) }
        lock.unlock()
    }

    // Last, so every field is initialized before the run loop thread starts.
    init {
        val thread = NSThread { runLoop() }
        thread.name = "l2cap-runloop"
        thread.start()
    }
}

// Kotlin/Native does not allow a class to extend NSObject and implement a Kotlin interface, so the
// socket cannot be its own stream delegate.
private class StreamDelegate : NSObject(), NSStreamDelegateProtocol {

    var onEvent: ((NSStream, NSStreamEvent) -> Unit)? = null

    override fun stream(aStream: NSStream, handleEvent: NSStreamEvent) {
        onEvent?.invoke(aStream, handleEvent)
    }
}
