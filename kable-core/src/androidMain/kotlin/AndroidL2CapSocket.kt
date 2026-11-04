package com.juul.kable

import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothSocketException
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class AndroidL2CapSocket(
    socket: BluetoothSocket,
) : L2CapSocket {
    private val inputStream = socket.inputStream
    private val outputStream = socket.outputStream

    override val isReady: StateFlow<Boolean> = MutableStateFlow(true)

    override suspend fun receive(maxBytesToRead: Int): ByteArray {
        try {
            val buffer = ByteArray(maxBytesToRead)
            val bytesRead = inputStream.read(buffer)
            return buffer.take(bytesRead).toByteArray()
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && e is BluetoothSocketException) {
                throw L2CapException(e.message, e, e.errorCode.toLong())
            } else {
                throw L2CapException(e.message, e, 0)
            }
        }
    }

    override suspend fun send(packet: ByteArray): Long {
        try {
            outputStream.write(packet)
            return packet.size.toLong()
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && e is BluetoothSocketException) {
                throw L2CapException(e.message, e, e.errorCode.toLong())
            } else {
                throw L2CapException(e.message, e, 0)
            }
        }
    }
}