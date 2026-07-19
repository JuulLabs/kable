package com.juul.kable

import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothSocketException
import android.os.Build
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

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

    private val _hasReachedEof = MutableStateFlow(false)
    override val hasReachedEof: StateFlow<Boolean> = _hasReachedEof.asStateFlow()

    override suspend fun read(buffer: ByteArray): Int {
        try {
            val count = withContext(Dispatchers.IO) {
                inputStream.read(buffer)
            }
            if (count < 0) {
                _hasReachedEof.value = true
                _isConnected.value = false
            }
            return count
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _isConnected.value = false
            logger.error(e) { message = "Failed to read bytes" }
            throw e.toL2CapException()
        }
    }

    override suspend fun write(packet: ByteArray) {
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
        _isConnected.value = false
        socket.close()
    }
}

internal fun Exception.toL2CapException(): L2CapException =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && this is BluetoothSocketException) {
        L2CapException(message, this, errorCode.toLong())
    } else {
        L2CapException(message, this, 0)
    }
