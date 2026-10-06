package com.juul.kable

import android.bluetooth.BluetoothSocket
import android.os.Build
import androidx.annotation.RequiresApi
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.ATOMIC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.Q)
internal class AndroidL2CapSocket(
    private val socket: BluetoothSocket,
    scope: CoroutineScope,
    logging: Logging,
) : L2CapSocket {

    private val logger = Logger(logging, "Kable/L2CapSocket", socket.remoteDevice.toString())

    private val _isConnected = MutableStateFlow(true)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val chunks = Channel<ByteArray>(UNLIMITED)
    override val incoming: SharedFlow<ByteArray> = chunks.receiveAsFlow().shareIn(scope, Lazily)

    private val guard = Mutex()

    // Blocking reads ignore coroutine cancellation, so the socket is closed when `scope` is cancelled.
    @Suppress("OPT_IN_USAGE")
    private val closer = scope.launch(start = ATOMIC) {
        try {
            awaitCancellation()
        } finally {
            _isConnected.value = false
            socket.close()
        }
    }

    init {
        scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(socket.maxReceivePacketSize)
            try {
                while (true) {
                    val count = socket.inputStream.read(buffer)
                    if (count < 0) break
                    chunks.send(buffer.copyOf(count))
                }
            } catch (e: Exception) {
                if (isConnected.value) logger.warn(e) { message = "Read failed" }
            } finally {
                chunks.close()
                closer.cancel()
            }
        }
    }

    override suspend fun write(packet: ByteArray) {
        require(packet.isNotEmpty()) { "Packet must not be empty" }
        guard.withLock {
            withContext(Dispatchers.IO) {
                socket.outputStream.write(packet)
            }
        }
    }

    override suspend fun close() {
        closer.cancelAndJoin()
    }
}
