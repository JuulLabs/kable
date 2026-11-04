package com.juul.kable

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.CoreBluetooth.CBL2CAPChannel
import platform.CoreFoundation.kCFStreamEventCanAcceptBytes
import platform.CoreFoundation.kCFStreamEventEndEncountered
import platform.CoreFoundation.kCFStreamEventErrorOccurred
import platform.CoreFoundation.kCFStreamEventHasBytesAvailable
import platform.CoreFoundation.kCFStreamEventOpenCompleted
import platform.Foundation.NSStream
import platform.Foundation.NSStreamDelegateProtocol
import platform.Foundation.NSStreamEvent
import platform.darwin.NSObject
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
internal class AppleL2CapSocket(private val channel: CBL2CAPChannel) : L2CapSocket {
    private val inputStream = channel.inputStream
    private val outputStream = channel.outputStream

    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady

    private var openChannels = AtomicInt(0)

    private val delegate = object : NSObject(), NSStreamDelegateProtocol {
        override fun stream(aStream: NSStream, handleEvent: NSStreamEvent) {
            super.stream(aStream, handleEvent)
            when (handleEvent) {
                kCFStreamEventOpenCompleted -> {
                    val value = openChannels.addAndFetch(1)
                    if (value == 2) _isReady.tryEmit(true)
                }

                kCFStreamEventHasBytesAvailable -> {}
                kCFStreamEventCanAcceptBytes -> {}
                kCFStreamEventErrorOccurred -> {}
                kCFStreamEventEndEncountered -> {}
            }
        }
    }

    init {
        inputStream?.open()
        outputStream?.open()
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun receive(maxBytesToRead: Int): ByteArray {
        val buffer = ByteArray(maxBytesToRead)
        val readBytes = buffer.usePinned { pinned ->
            channel.inputStream?.read(
                pinned.addressOf(0).reinterpret(),
                maxBytesToRead.toULong(),
            ) ?: -1
        }
        return if (readBytes > 0) buffer.copyOf(readBytes.toInt()) else ByteArray(0)
    }


    @OptIn(ExperimentalForeignApi::class)
    override suspend fun send(packet: ByteArray): Long {
        val writeBytes = channel.outputStream?.write(packet.toCPointer(), packet.size.toULong())
        if (writeBytes != null) {
            return writeBytes
        } else {
            throw L2CapException("couldn't send packet", code = 0L)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ByteArray.toCPointer(): CPointer<UByteVar>? {
        memScoped {
            usePinned {
                return memScope.allocArray<UByteVar>(this@toCPointer.size).also { cArray ->
                    for (i in indices) {
                        cArray[i] = this@toCPointer[i].toUByte()
                    }
                }
            }
        }
    }
}