@file:OptIn(ExperimentalForeignApi::class)

package com.juul.kable

import com.juul.kable.logs.Logging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.runningReduce
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import platform.CoreBluetooth.CBL2CAPChannel
import platform.CoreFoundation.CFReadStreamRefVar
import platform.CoreFoundation.CFStreamCreateBoundPair
import platform.CoreFoundation.CFWriteStreamRefVar
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSInputStream
import platform.Foundation.NSOutputStream
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
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

// Also the channel, so the peer's stream ends stay alive as long as the socket holds the channel.
private class Peer(bufferSize: Int = 256 * 1024) : CBL2CAPChannel() {
    private val toSocket = BoundStreamPair(bufferSize)
    private val fromSocket = BoundStreamPair(bufferSize)

    init {
        toSocket.output.open()
        fromSocket.input.open()
    }

    override fun inputStream(): NSInputStream? = toSocket.input
    override fun outputStream(): NSOutputStream? = fromSocket.output

    fun send(bytes: ByteArray) {
        var offset = 0
        while (offset < bytes.size) {
            val written = bytes.usePinned { pinned ->
                toSocket.output.write(pinned.addressOf(offset).reinterpret(), (bytes.size - offset).convert())
            }.convert<Int>()
            check(written > 0) { "Peer send failed: $written" }
            offset += written
        }
    }

    /** Blocks until [count] bytes have been received. */
    fun receive(count: Int): ByteArray {
        val bytes = ByteArray(count)
        var offset = 0
        while (offset < count) {
            val read = bytes.usePinned { pinned ->
                fromSocket.input.read(pinned.addressOf(offset).reinterpret(), (count - offset).convert())
            }.convert<Int>()
            check(read > 0) { "Peer receive failed: $read" }
            offset += read
        }
        return bytes
    }

    fun endOfStream() {
        toSocket.output.close()
    }
}

private suspend fun Flow<ByteArray>.receive(count: Int): ByteArray =
    runningReduce(ByteArray::plus).first { it.size >= count }

class AppleL2CapSocketTests {

    private fun TestScope.socket(peer: Peer, scope: CoroutineScope = backgroundScope) =
        AppleL2CapSocket(peer, scope, Logging())

    @Test
    fun incoming_dataReceivedBeforeFirstCollector_isDelivered() = runTest(timeout = 5.seconds) {
        val peer = Peer()
        val socket = socket(peer)
        peer.send(byteArrayOf(1, 2, 3))

        assertContentEquals(byteArrayOf(1, 2, 3), socket.incoming.receive(3))
        socket.close()
    }

    @Test
    fun incoming_multipleCollectors_eachReceiveEveryChunk() = runTest(timeout = 5.seconds) {
        val peer = Peer()
        val socket = socket(peer)
        val collectors = List(2) {
            async(start = UNDISPATCHED) { socket.incoming.receive(2) }
        }
        peer.send(byteArrayOf(1, 2))

        collectors.awaitAll().forEach { assertContentEquals(byteArrayOf(1, 2), it) }
        socket.close()
    }

    @Test
    fun endOfStream_disconnects() = runTest(timeout = 5.seconds) {
        val peer = Peer()
        val socket = socket(peer)
        peer.endOfStream()

        socket.isConnected.first { !it }
        socket.close()
    }

    @Test
    fun scopeCancelled_disconnects() = runTest(timeout = 5.seconds) {
        val peer = Peer()
        val connectionScope = CoroutineScope(backgroundScope.coroutineContext + Job())
        val socket = socket(peer, connectionScope)

        connectionScope.cancel()
        socket.isConnected.first { !it }
    }

    @Test
    fun write_concurrently_deliversEachPacketWhole() = runTest(timeout = 5.seconds) {
        val peer = Peer(bufferSize = 64)
        val socket = socket(peer)
        val packets = List(3) { index -> ByteArray(1024) { index.toByte() } }

        val received = async(Dispatchers.Default) { peer.receive(packets.sumOf { it.size }) }
        packets.map { packet -> async { socket.write(packet) } }.awaitAll()

        val chunks = received.await().toList().chunked(1024)
        assertContentEquals(
            packets.map { it.first() }.sorted(),
            chunks.map { chunk -> chunk.distinct().single() }.sorted(),
        )
        socket.close()
    }

    @Test
    fun close_whileWriteBlocked_failsWrite() = runTest(timeout = 5.seconds) {
        val peer = Peer(bufferSize = 64)
        val socket = socket(peer)

        val write = async { runCatching { socket.write(ByteArray(1024)) } }
        withContext(Dispatchers.Default) { peer.receive(1) }
        socket.close()
        assertFailsWith<IOException> { write.await().getOrThrow() }
    }

    @Test
    fun write_emptyPacket_throwsIllegalArgumentException() = runTest(timeout = 5.seconds) {
        val peer = Peer()
        val socket = socket(peer)

        assertFailsWith<IllegalArgumentException> { socket.write(byteArrayOf()) }
        socket.close()
    }
}
