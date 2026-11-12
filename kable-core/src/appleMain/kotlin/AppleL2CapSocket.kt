@file:OptIn(ExperimentalForeignApi::class, NativeRuntimeApi::class)

package com.juul.kable

import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.logs.detail
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.ATOMIC
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import platform.CoreBluetooth.CBL2CAPChannel
import platform.CoreFoundation.CFRunLoopGetCurrent
import platform.CoreFoundation.CFRunLoopPerformBlock
import platform.CoreFoundation.CFRunLoopRun
import platform.CoreFoundation.CFRunLoopStop
import platform.CoreFoundation.CFRunLoopWakeUp
import platform.CoreFoundation.kCFRunLoopDefaultMode
import platform.Foundation.NSDefaultRunLoopMode
import platform.Foundation.NSRunLoop
import platform.Foundation.NSStream
import platform.Foundation.NSStreamDelegateProtocol
import platform.Foundation.NSStreamEvent
import platform.Foundation.NSStreamEventEndEncountered
import platform.Foundation.NSStreamEventErrorOccurred
import platform.Foundation.NSStreamEventHasBytesAvailable
import platform.Foundation.NSStreamEventHasSpaceAvailable
import platform.Foundation.NSThread
import platform.darwin.NSObject
import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi

// Upper bound for a single chunk, as CoreBluetooth does not expose the channel's MTU.
private const val READ_BUFFER_SIZE = 8192

// Streams deliver events through the run loop they are scheduled on, and coroutine dispatcher threads
// do not run one, so the streams are only used from a dedicated thread that does:
// https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/Streams/Articles/ReadingInputStreams.html
internal class AppleL2CapSocket(
    channel: CBL2CAPChannel,
    scope: CoroutineScope,
    logging: Logging,
) : L2CapSocket {

    private val logger = Logger(logging, "Kable/L2CapSocket", channel.peer?.identifier?.UUIDString)

    // CoreBluetooth closes the channel when the `CBL2CAPChannel` is deallocated, not before.
    private var channel: CBL2CAPChannel? = channel
    private val inputStream = checkNotNull(channel.inputStream) { "L2CAP channel has no input stream" }
    private val outputStream = checkNotNull(channel.outputStream) { "L2CAP channel has no output stream" }

    private val _isConnected = MutableStateFlow(true)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val chunks = Channel<ByteArray>(UNLIMITED)
    override val incoming: SharedFlow<ByteArray> = chunks.receiveAsFlow().shareIn(scope, Lazily)
    private val readBuffer = ByteArray(READ_BUFFER_SIZE)

    private class PendingWrite(val packet: ByteArray) {
        var offset = 0
        val done = CompletableDeferred<Unit>()
    }

    private val guard = Mutex()
    private val writes = Channel<PendingWrite>(UNLIMITED)
    private var pendingWrite: PendingWrite? = null

    private val runLoop = CompletableDeferred<NSRunLoop>()
    private val stopped = CompletableDeferred<Unit>()

    private val delegate = object : NSObject(), NSStreamDelegateProtocol {
        override fun stream(aStream: NSStream, handleEvent: NSStreamEvent) {
            when (handleEvent) {
                NSStreamEventHasBytesAvailable -> read()
                NSStreamEventHasSpaceAvailable -> pump()
                NSStreamEventErrorOccurred -> {
                    logger.warn {
                        message = "Stream error"
                        detail(aStream.streamError)
                    }
                    stop()
                }
                NSStreamEventEndEncountered -> stop()
            }
        }
    }

    @Suppress("OPT_IN_USAGE")
    private val closer = scope.launch(start = ATOMIC) {
        try {
            awaitCancellation()
        } finally {
            withContext(NonCancellable) {
                if (!stopped.isCompleted) perform(::stop)
                stopped.await()
            }
        }
    }

    // Started last, as the thread uses the properties above.
    init {
        NSThread { run() }.apply { name = "Kable/L2CapSocket" }.start()
    }

    private fun run() {
        val currentRunLoop = NSRunLoop.currentRunLoop
        for (stream in listOf(inputStream, outputStream)) {
            stream.delegate = delegate
            stream.scheduleInRunLoop(currentRunLoop, NSDefaultRunLoopMode)
            stream.open()
        }
        runLoop.complete(currentRunLoop)
        CFRunLoopRun()

        channel = null
        // Kotlin/Native releases Objective-C objects only when collected, and until the
        // `CBL2CAPChannel` is released, reopening its PSM fails with "L2CAP PSM already connected".
        GC.collect()
        stopped.complete(Unit)
        closer.cancel()
    }

    // `CFRunLoopPerformBlock` does not wake the run loop:
    // https://developer.apple.com/documentation/corefoundation/cfrunloopperformblock(_:_:_:)
    private suspend fun perform(action: () -> Unit) {
        val cfRunLoop = runLoop.await().getCFRunLoop()
        CFRunLoopPerformBlock(cfRunLoop, kCFRunLoopDefaultMode, action)
        CFRunLoopWakeUp(cfRunLoop)
    }

    private fun read() {
        while (inputStream.hasBytesAvailable) {
            val count = readBuffer.usePinned { pinned ->
                inputStream.read(pinned.addressOf(0).reinterpret(), readBuffer.size.convert())
            }.convert<Int>()
            if (count <= 0) {
                stop()
                return
            }
            chunks.trySend(readBuffer.copyOf(count))
        }
    }

    private fun pump() {
        while (outputStream.hasSpaceAvailable) {
            val write = pendingWrite ?: writes.tryReceive().getOrNull() ?: return
            pendingWrite = write
            val count = write.packet.usePinned { pinned ->
                val remaining = write.packet.size - write.offset
                outputStream.write(pinned.addressOf(write.offset).reinterpret(), remaining.convert())
            }.convert<Int>()
            if (count < 0) stop()
            if (count <= 0) return
            write.offset += count
            if (write.offset == write.packet.size) {
                write.done.complete(Unit)
                pendingWrite = null
            }
        }
    }

    private fun stop() {
        if (!_isConnected.value) return
        _isConnected.value = false
        chunks.close()

        writes.close()
        val cause = IOException("L2CAP socket closed")
        pendingWrite?.done?.completeExceptionally(cause)
        pendingWrite = null
        generateSequence { writes.tryReceive().getOrNull() }.forEach { it.done.completeExceptionally(cause) }

        for (stream in listOf(inputStream, outputStream)) {
            stream.delegate = null
            stream.removeFromRunLoop(NSRunLoop.currentRunLoop, NSDefaultRunLoopMode)
            stream.close()
        }
        CFRunLoopStop(CFRunLoopGetCurrent())
    }

    override suspend fun write(packet: ByteArray) {
        require(packet.isNotEmpty()) { "Packet must not be empty" }
        guard.withLock {
            val write = PendingWrite(packet)
            if (writes.trySend(write).isFailure) throw IOException("L2CAP socket closed")
            perform(::pump)
            write.done.await()
        }
    }

    override suspend fun close() {
        closer.cancelAndJoin()
    }
}
