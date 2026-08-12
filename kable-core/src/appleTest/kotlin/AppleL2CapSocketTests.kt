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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

// An in-memory stand-in for the peer: data written to [output] becomes available on [input].
// CFStreamCreateBoundPair because NSStream's bound-pair factory lives in a category the
// Kotlin/Native bindings do not expose; the CF streams toll-free bridge to NSStreams.
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

// Holds both entire bound pairs — not just the socket-side ends — so the socket's retained
// channel keeps the peer-side stream ends alive too. A peer-side end collected mid-test (the
// Kotlin/Native GC is free to reclaim a local after its last use) deallocates half of a bound
// pair, which CFStream reports to the surviving half as end-of-stream.
private class FakeL2CapChannel(
    private val toSocket: BoundStreamPair,
    private val fromSocket: BoundStreamPair,
) : CBL2CAPChannel() {
    override fun inputStream(): NSInputStream? = toSocket.input
    override fun outputStream(): NSOutputStream? = fromSocket.output
}

// Test-side handle on the socket's inbound traffic. [bufferSize] must exceed the total a test
// sends without concurrent draining, as [send] fails rather than block on a full buffer.
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

    // The contract under test: cancelling read() never loses nor reorders data — a chunk orphaned
    // by cancellation (won after the chunk left the internal channel) must be returned by the next
    // read(). Cancellation racing delivery is inherently timing-dependent, so this hammers the race
    // and asserts the invariant that holds regardless of which side wins each round.
    @Test
    fun cancelledReads_loseNoData() = runBlocking {
        val total = 128 * 1024
        val sent = ByteArray(total) { (it % 251).toByte() }
        val peer = Peer()
        val socket = AppleL2CapSocket(peer.channel)
        try {
            val sender = launch(Dispatchers.Default) {
                var offset = 0
                while (offset < total) {
                    val burst = minOf(1024, total - offset)
                    peer.send(sent.copyOfRange(offset, offset + burst))
                    offset += burst
                    delay(1)
                }
            }
            val received = ByteArray(total)
            var receivedCount = 0
            withTimeout(30.seconds) {
                // Reader and this loop share runBlocking's single thread, so receivedCount is
                // not touched concurrently.
                while (receivedCount < total) {
                    val reader = launch {
                        while (receivedCount < total) {
                            val chunk = socket.read() ?: error("Unexpected end of stream")
                            chunk.copyInto(received, receivedCount)
                            receivedCount += chunk.size
                        }
                    }
                    delay(2)
                    reader.cancelAndJoin()
                }
                sender.join()
            }
            assertContentEquals(sent, received)
        } finally {
            socket.close()
        }
    }

    @Test
    fun read_afterEndOfStream_returnsNullAndStaysNull() = runBlocking {
        val peer = Peer()
        val socket = AppleL2CapSocket(peer.channel)
        try {
            val sent = byteArrayOf(6, 7, 8)
            peer.send(sent)
            peer.endOfStream()
            // Data sent before end-of-stream is still delivered ahead of the null.
            val received = withTimeout(5.seconds) { socket.read() }
            assertContentEquals(sent, received)
            assertNull(withTimeout(5.seconds) { socket.read() })
            assertNull(withTimeout(5.seconds) { socket.read() })
            assertFalse(socket.isConnected.value)
        } finally {
            socket.close()
        }
    }
}
