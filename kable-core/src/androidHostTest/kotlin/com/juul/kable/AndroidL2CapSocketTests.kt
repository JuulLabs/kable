package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Build
import com.juul.kable.logs.Logging
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

// Scriptable stand-in for BluetoothSocket's InputStream: read(bytes) blocks until the test feeds
// data, end-of-stream, or a failure — mirroring the non-interruptible blocking read of the real
// socket. [readEntered] lets a test wait until a read is genuinely in flight, and [readsStarted]
// counts reads handed to the "OS" so tests can pin how far the socket drained.
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
        // The real stream makes a blocked read throw once the socket closes underneath it.
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

    private fun socket(): AndroidL2CapSocket {
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
        return AndroidL2CapSocket(bluetoothSocket, Logging())
    }

    // The contract under test: cancelling read() never loses data. The blocking read a cancelled
    // read() leaves in flight is awaited by the next read() — not abandoned, and not doubled up
    // with a second OS read.
    @Test
    fun cancelledRead_dataIsReturnedByNextRead() = runBlocking {
        val socket = socket()
        // UNDISPATCHED so the reader reaches its suspension point before this thread — the only
        // one in runBlocking — blocks in awaitReadInFlight.
        val reader = launch(start = UNDISPATCHED) { socket.read() }
        input.awaitReadInFlight()
        reader.cancel()
        reader.join()

        val sent = byteArrayOf(4, 5, 6)
        input.feed(sent)
        assertContentEquals(sent, withTimeout(5.seconds) { socket.read() })
        assertEquals(1, input.readsStarted.get(), "Expected the pending read to be reused")
        socket.close()
    }

    // A slow consumer must throttle the peer via L2CAP flow control: the socket reads from the OS
    // only on demand, never draining ahead of read() calls.
    @Test
    fun read_doesNotDrainAheadOfConsumer() = runBlocking {
        val socket = socket()
        input.feed(byteArrayOf(1))
        input.feed(byteArrayOf(2))

        assertContentEquals(byteArrayOf(1), withTimeout(5.seconds) { socket.read() })
        assertEquals(1, input.readsStarted.get(), "Read ahead of the consumer")

        assertContentEquals(byteArrayOf(2), withTimeout(5.seconds) { socket.read() })
        assertEquals(2, input.readsStarted.get())
        socket.close()
    }

    @Test
    fun read_afterEndOfStream_returnsNullAndStaysNull() = runBlocking {
        val socket = socket()
        input.feed(byteArrayOf(7))
        input.endOfStream()

        assertContentEquals(byteArrayOf(7), withTimeout(5.seconds) { socket.read() })
        assertNull(withTimeout(5.seconds) { socket.read() })
        assertFalse(socket.isConnected.value)
        // End-of-stream is terminal: no further OS reads on a dead socket.
        assertNull(withTimeout(5.seconds) { socket.read() })
        assertEquals(2, input.readsStarted.get())
        socket.close()
    }

    @Test
    fun readFailure_throwsAndSubsequentReadsObserveSameFailure() = runBlocking {
        val socket = socket()
        input.failWith(IOException("peer vanished"))

        assertFailsWith<L2CapException> { withTimeout(5.seconds) { socket.read() } }
        assertFalse(socket.isConnected.value)
        // Failure is terminal: rethrown without starting another read on a dead socket.
        assertFailsWith<L2CapException> { withTimeout(5.seconds) { socket.read() } }
        assertEquals(1, input.readsStarted.get())
        socket.close()
    }

    @Test
    fun close_whileReadBlocked_completesReadWithNull() = runBlocking {
        val socket = socket()
        val reader = launch(start = UNDISPATCHED) {
            assertNull(socket.read())
        }
        input.awaitReadInFlight()
        withTimeout(5.seconds) { socket.close() }
        withTimeout(5.seconds) { reader.join() }
        assertTrue(reader.isCompleted)
        assertFalse(socket.isConnected.value)
    }
}
