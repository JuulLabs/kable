package com.juul.kable

import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothSocketException
import android.os.Build
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

private const val READ_CHUNK_SIZE = 8192

internal class AndroidL2CapSocket(
    private val socket: BluetoothSocket,
    scope: CoroutineScope,
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

    private val _isConnected = MutableStateFlow(true)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    @Volatile
    private var closed = false

    // Unbuffered, so the socket is only read as fast as `incoming` is collected.
    private val chunks = Channel<ByteArray>()
    override val incoming: Flow<ByteArray> = chunks.receiveAsFlow()

    private val reader = scope.launch(Dispatchers.IO) {
        val buffer = ByteArray(READ_CHUNK_SIZE)
        try {
            while (true) {
                val count = socket.inputStream.read(buffer)
                if (count < 0) break
                chunks.send(buffer.copyOf(count))
            }
        } catch (e: IOException) {
            if (!closed) chunks.close(e.wrapInL2CapException())
        } finally {
            _isConnected.value = false
            chunks.close()
        }
    }

    override suspend fun write(packet: ByteArray) {
        require(packet.isNotEmpty()) { "Packet must not be empty" }
        try {
            withContext(Dispatchers.IO) {
                socket.outputStream.write(packet)
            }
        } catch (e: IOException) {
            _isConnected.value = false
            throw e.wrapInL2CapException()
        }
    }

    override suspend fun close() {
        closed = true
        _isConnected.value = false
        withContext(NonCancellable + Dispatchers.IO) {
            socket.close()
            reader.cancelAndJoin()
        }
    }
}

internal fun IOException.wrapInL2CapException(): L2CapException {
    val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && this is BluetoothSocketException) {
        errorCode.toLong()
    } else {
        null
    }
    return L2CapException(message, this, code)
}
