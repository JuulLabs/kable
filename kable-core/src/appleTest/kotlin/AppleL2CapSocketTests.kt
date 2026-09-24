@file:OptIn(ExperimentalForeignApi::class)

package com.juul.kable

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import platform.CoreBluetooth.CBL2CAPChannel
import platform.CoreFoundation.CFReadStreamRefVar
import platform.CoreFoundation.CFStreamCreateBoundPair
import platform.CoreFoundation.CFWriteStreamRefVar
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSInputStream
import platform.Foundation.NSOutputStream
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.seconds

private class BoundStreamPair(bufferSize: Int) {
    val input: NSInputStream
    val output: NSOutputStream

    init {
        memScoped {
            val readVar = alloc<CFReadStreamRefVar>()
            val writeVar = alloc<CFWriteStreamRefVar>()
            CFStreamCreateBoundPair(null, readVar.ptr, writeVar.ptr, bufferSize.convert())
            input = CFBridgingRelease(readVar.value) as NSInputStream
            output = CFBridgingRelease(writeVar.value) as NSOutputStream
        }
    }
}

// Holds both pairs, so the peer side stays alive as long as the channel does.
private class FakeL2CapChannel(
    private val toSocket: BoundStreamPair,
    private val fromSocket: BoundStreamPair,
) : CBL2CAPChannel() {
    override fun inputStream(): NSInputStream? = toSocket.input
    override fun outputStream(): NSOutputStream? = fromSocket.output
}

private class Peer(bufferSize: Int = 256 * 1024) {
    private val toSocket = BoundStreamPair(bufferSize)
    private val fromSocket = BoundStreamPair(bufferSize)

    val channel = FakeL2CapChannel(toSocket, fromSocket)

    init {
        toSocket.output.open()
    }

    fun send(bytes: ByteArray) {
        var offset = 0
        while (offset < bytes.size) {
            val written = bytes.usePinned { pinned ->
                toSocket.output.write(pinned.addressOf(offset).reinterpret(), (bytes.size - offset).convert())
            }.toLong()
            check(written > 0L) { "Peer send failed: $written" }
            offset += written.toInt()
        }
    }

    fun endOfStream() {
        toSocket.output.close()
    }
}

class AppleL2CapSocketTests {

    @Test
    fun incoming_deliversDataThenCompletesAtEndOfStream() = runTest(timeout = 5.seconds) {
        val peer = Peer()
        val socket = AppleL2CapSocket(peer.channel)
        try {
            val sent = byteArrayOf(6, 7, 8)
            peer.send(sent)
            peer.endOfStream()
            val received = socket.incoming.toList().reduce(ByteArray::plus)
            assertContentEquals(sent, received)
            assertFalse(socket.isConnected.value)
        } finally {
            socket.close()
        }
    }

    @Test
    fun incoming_collectedAgain_continuesWhereItLeftOff() = runTest(timeout = 5.seconds) {
        val peer = Peer()
        val socket = AppleL2CapSocket(peer.channel)
        try {
            peer.send(byteArrayOf(1))
            assertContentEquals(byteArrayOf(1), socket.incoming.first())
            peer.send(byteArrayOf(2))
            assertContentEquals(byteArrayOf(2), socket.incoming.first())
        } finally {
            socket.close()
        }
    }
}
