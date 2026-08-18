package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Build
import com.juul.kable.logs.Logging
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit.SECONDS
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
        data class Failure(val exception: IOException) : Outcome
    }

    private val outcomes = LinkedBlockingQueue<Outcome>()
    private val readEntered = Semaphore(0)
    val readsStarted = AtomicInteger()

    override fun read(): Int = error("Single-byte read not expected")

    override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
        readsStarted.incrementAndGet()
        readEntered.release()
        return when (val outcome = outcomes.take()) {
            is Outcome.Data -> {
                check(outcome.bytes.size <= length) { "Fed chunk larger than read buffer" }
                outcome.bytes.copyInto(bytes, offset)
                outcome.bytes.size
            }
            Outcome.Eof -> -1
            is Outcome.Failure -> throw outcome.exception
        }
    }

    override fun close() {
        outcomes.put(Outcome.Failure(IOException("Socket closed")))
    }

    fun feed(bytes: ByteArray) = outcomes.put(Outcome.Data(bytes))
    fun endOfStream() = outcomes.put(Outcome.Eof)
    fun failWith(exception: IOException) = outcomes.put(Outcome.Failure(exception))

    fun awaitReadInFlight() {
        check(readEntered.tryAcquire(5, SECONDS)) { "Timed out waiting for a read to start" }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class AndroidL2CapSocketTests {

    private val input = ScriptedInputStream()

    private fun TestScope.socket(): AndroidL2CapSocket {
        val bluetoothSocket = mockk<BluetoothSocket> {
            every { remoteDevice } returns mockk<BluetoothDevice> {
                every { address } returns "00:11:22:AA:BB:CC"
            }
            every { maxTransmitPacketSize } returns 512
            every { maxReceivePacketSize } returns 512
            every { inputStream } returns input
            every { outputStream } returns mockk<OutputStream>()
            every { close() } answers { input.close() }
        }
        return AndroidL2CapSocket(bluetoothSocket, backgroundScope, Logging())
    }

    @Test
    fun incoming_deliversChunksThenCompletesAtEndOfStream() = runTest(timeout = 5.seconds) {
        val socket = socket()
        input.feed(byteArrayOf(1, 2))
        input.feed(byteArrayOf(3))
        input.endOfStream()

        val chunks = socket.incoming.toList()
        assertEquals(2, chunks.size)
        assertContentEquals(byteArrayOf(1, 2, 3), chunks.reduce(ByteArray::plus))
        assertFalse(socket.isConnected.value)
        socket.close()
    }

    @Test
    fun incoming_collectedAgain_continuesWhereItLeftOff() = runTest(timeout = 5.seconds) {
        val socket = socket()
        input.feed(byteArrayOf(1))
        input.feed(byteArrayOf(2))

        assertContentEquals(byteArrayOf(1), socket.incoming.first())
        assertContentEquals(byteArrayOf(2), socket.incoming.first())
        socket.close()
    }

    @Test
    fun socket_doesNotReadAheadOfCollector() = runTest(timeout = 5.seconds) {
        val socket = socket()
        input.feed(byteArrayOf(1))
        input.feed(byteArrayOf(2))
        input.awaitReadInFlight()

        assertEquals(1, input.readsStarted.get())
        assertContentEquals(byteArrayOf(1), socket.incoming.first())
        socket.close()
    }

    @Test
    fun incoming_failure_throwsL2CapException() = runTest(timeout = 5.seconds) {
        val socket = socket()
        input.failWith(IOException("peer vanished"))

        assertFailsWith<L2CapException> { socket.incoming.toList() }
        assertFalse(socket.isConnected.value)
        socket.close()
    }

    @Test
    fun close_whileReadBlocked_completesIncoming() = runTest(timeout = 5.seconds) {
        val socket = socket()
        val collector = launch { socket.incoming.toList() }
        input.awaitReadInFlight()

        socket.close()
        collector.join()
        assertFalse(socket.isConnected.value)
    }

    @Test
    fun write_emptyPacket_throwsIllegalArgumentException() = runTest(timeout = 5.seconds) {
        val socket = socket()
        assertFailsWith<IllegalArgumentException> { socket.write(byteArrayOf()) }
        socket.close()
    }
}
