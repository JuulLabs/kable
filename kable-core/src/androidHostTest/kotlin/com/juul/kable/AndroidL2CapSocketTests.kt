package com.juul.kable

import android.bluetooth.BluetoothSocket
import android.os.Build
import com.juul.kable.logs.Logging
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.seconds

private class ScriptedInputStream : InputStream() {

    private sealed interface Outcome {
        data class Data(val bytes: ByteArray) : Outcome
        object Eof : Outcome
        data class Failure(val exception: Exception) : Outcome
    }

    private val outcomes = LinkedBlockingQueue<Outcome>()

    override fun read(): Int = error("Single-byte read not expected")

    override fun read(bytes: ByteArray, offset: Int, length: Int): Int =
        when (val outcome = outcomes.take()) {
            is Outcome.Data -> {
                outcome.bytes.copyInto(bytes, offset)
                outcome.bytes.size
            }
            Outcome.Eof -> -1
            is Outcome.Failure -> throw outcome.exception
        }

    override fun close() {
        outcomes.put(Outcome.Failure(IOException("Socket closed")))
    }

    fun feed(bytes: ByteArray) = outcomes.put(Outcome.Data(bytes))
    fun endOfStream() = outcomes.put(Outcome.Eof)
    fun failWith(exception: Exception) = outcomes.put(Outcome.Failure(exception))
}

private class ConcurrencyTrackingOutputStream : OutputStream() {

    private val active = AtomicInteger()
    val maxActive = AtomicInteger()

    override fun write(b: Int) = error("Single-byte write not expected")

    override fun write(bytes: ByteArray, offset: Int, length: Int) {
        maxActive.accumulateAndGet(active.incrementAndGet(), ::maxOf)
        Thread.sleep(10)
        active.decrementAndGet()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class AndroidL2CapSocketTests {

    private val input = ScriptedInputStream()
    private val output = ConcurrencyTrackingOutputStream()
    private val bluetoothSocket = mockk<BluetoothSocket> {
        every { remoteDevice } returns mockk()
        every { maxReceivePacketSize } returns 512
        every { inputStream } returns input
        every { outputStream } returns output
        every { close() } answers { input.close() }
    }

    private fun TestScope.socket(scope: CoroutineScope = backgroundScope) =
        AndroidL2CapSocket(bluetoothSocket, scope, Logging())

    @Test
    fun incoming_chunksReceivedBeforeFirstCollector_areDelivered() = runTest(timeout = 5.seconds) {
        val socket = socket()
        input.feed(byteArrayOf(1, 2))
        input.feed(byteArrayOf(3))

        val chunks = socket.incoming.take(2).toList()
        assertContentEquals(byteArrayOf(1, 2, 3), chunks.reduce(ByteArray::plus))
        socket.close()
    }

    @Test
    fun incoming_multipleCollectors_eachReceiveEveryChunk() = runTest(timeout = 5.seconds) {
        val socket = socket()
        val collectors = List(2) {
            async(start = UNDISPATCHED) { socket.incoming.take(2).toList() }
        }
        input.feed(byteArrayOf(1))
        input.feed(byteArrayOf(2))

        collectors.awaitAll().forEach { chunks ->
            assertContentEquals(byteArrayOf(1, 2), chunks.reduce(ByteArray::plus))
        }
        socket.close()
    }

    @Test
    fun endOfStream_closesSocket() = runTest(timeout = 5.seconds) {
        val socket = socket()
        input.endOfStream()

        socket.isConnected.first { !it }
        verify { bluetoothSocket.close() }
    }

    @Test
    fun readFailure_closesSocket() = runTest(timeout = 5.seconds) {
        val socket = socket()
        input.failWith(IllegalStateException("Unexpected"))

        socket.isConnected.first { !it }
        verify { bluetoothSocket.close() }
    }

    @Test
    fun scopeCancelled_closesSocket() = runTest(timeout = 5.seconds) {
        val connectionScope = CoroutineScope(backgroundScope.coroutineContext + Job())
        val socket = socket(connectionScope)

        connectionScope.cancel()
        socket.isConnected.first { !it }
        verify { bluetoothSocket.close() }
    }

    @Test
    fun close_whileReadBlocked_closesSocket() = runTest(timeout = 5.seconds) {
        val socket = socket()

        socket.close()
        assertFalse(socket.isConnected.value)
        verify { bluetoothSocket.close() }
    }

    @Test
    fun write_concurrently_doesNotInterleave() = runTest(timeout = 5.seconds) {
        val socket = socket()

        List(3) { async { socket.write(byteArrayOf(1, 2, 3)) } }.awaitAll()
        assertEquals(1, output.maxActive.get())
        socket.close()
    }

    @Test
    fun write_emptyPacket_throwsIllegalArgumentException() = runTest(timeout = 5.seconds) {
        val socket = socket()
        assertFailsWith<IllegalArgumentException> { socket.write(byteArrayOf()) }
        socket.close()
    }
}
