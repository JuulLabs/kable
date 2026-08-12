package com.juul.kable

import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothSocketException
import android.os.Build
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

private const val READ_CHUNK_SIZE = 8192

internal class AndroidL2CapSocket(
    private val socket: BluetoothSocket,
    logging: Logging,
) : L2CapSocket {

    private val logger = Logger(logging, "Kable/L2CapSocket", socket.remoteDevice.address)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            logger.info {
                message = "L2CAP MTU tx=${socket.maxTransmitPacketSize}, rx=${socket.maxReceivePacketSize}"
            }
        }
    }

    private val inputStream = socket.inputStream
    private val outputStream = socket.outputStream

    private val _isConnected = MutableStateFlow(true)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Lets the read path report the exception from a deliberate socket.close() as end-of-stream.
    @Volatile
    private var closeRequested = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // The in-flight blocking read. It survives a cancelled read() — data the OS may already have handed
    // over must not be lost — so the next read() awaits it instead of starting another. Only touched by
    // read(), which the interface limits to a single coroutine at a time.
    private var pending: Deferred<ByteArray?>? = null

    override suspend fun read(): ByteArray? {
        val chunk = pending ?: scope.async { readChunk() }.also { pending = it }
        try {
            val result = chunk.await()
            // End-of-stream and failure are terminal: they stay in pending so every later read()
            // observes the same outcome instead of starting another read on a dead socket.
            if (result != null) pending = null
            return result
        } catch (e: CancellationException) {
            // Rethrows when read() itself was cancelled (pending is kept for the next read());
            // otherwise close() cancelled the scope, which is end-of-stream.
            currentCoroutineContext().ensureActive()
            return null
        }
    }

    // Blocking read into a socket-owned array, so a cancelled read() never leaves the OS writing into a
    // caller's buffer. Terminal state is recorded here so it is observed even when no read() is awaiting.
    private fun readChunk(): ByteArray? {
        val scratch = ByteArray(READ_CHUNK_SIZE)
        val count = try {
            inputStream.read(scratch)
        } catch (e: Exception) {
            _isConnected.value = false
            if (closeRequested) return null
            logger.error(e) { message = "Failed to read bytes" }
            throw e.toL2CapException()
        }
        if (count < 0 || closeRequested) {
            _isConnected.value = false
            return null
        }
        return if (count == scratch.size) scratch else scratch.copyOf(count)
    }

    override suspend fun write(packet: ByteArray) {
        if (packet.isEmpty()) return
        try {
            withContext(Dispatchers.IO) {
                outputStream.write(packet)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _isConnected.value = false
            logger.error(e) { message = "Failed to write packet" }
            throw e.toL2CapException()
        }
    }

    override suspend fun close() {
        closeRequested = true
        _isConnected.value = false
        scope.cancel() // Completes any awaiting read() with null, even if socket.close() below throws.
        withContext(NonCancellable + Dispatchers.IO) {
            try {
                socket.close() // Unblocks an in-flight readChunk.
            } finally {
                scope.coroutineContext.job.join()
            }
        }
    }
}

internal fun Exception.toL2CapException(): L2CapException =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && this is BluetoothSocketException) {
        L2CapException(message, this, errorCode.toLong())
    } else {
        L2CapException(message, this, 0)
    }
