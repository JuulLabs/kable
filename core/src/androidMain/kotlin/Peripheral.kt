package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.content.Context
import com.juul.kable.Event.Rejected
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class NotReadyException internal constructor(
    message: String
) : IOException(message)

internal fun CoroutineScope.peripheral(
    androidContext: Context,
    bluetoothDevice: BluetoothDevice,
) = Peripheral(coroutineContext, androidContext, bluetoothDevice)

public actual class Peripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val androidContext: Context,
    private val bluetoothDevice: BluetoothDevice,
) {

    private val scope =
        CoroutineScope(parentCoroutineContext + Job(parentCoroutineContext[Job]))

    private val _state = MutableStateFlow<State?>(null)
    public actual val state: Flow<State> = _state.filterNotNull()

    private val _events = MutableSharedFlow<Event>()
    public actual val events: Flow<Event> = _events.asSharedFlow()

    private val observers = Observers(this)

    @Volatile
    private var _connection: Connection? = null
    private val connection: Connection
        inline get() = _connection ?: throw NotReadyException(toString())

    public actual val services: List<DiscoveredService>?
        get() = TODO("Not yet implemented")

    public actual suspend fun rssi(): Int {
        TODO("Not yet implemented")
    }

    public actual suspend fun connect() {
        // todo: Prevent multiple simultaneous connection attempts.

        val connection = bluetoothDevice.connect(androidContext)
        if (connection == null) {
            _events.emit(Rejected)
            return
        }

        // todo: Cancel collection on connection drop. via upstream Channel?
        connection.callback
            .onCharacteristicChanged
            .onEach(observers.characteristicChanges::emit)
            .launchIn(scope, start = UNDISPATCHED)

        connection.suspendUntilConnected()
        discoverServices() || error("Service discovery failed")
        observers.rewire()
        _connection = connection
        _events.emit(Event.Connected(this))
    }

    public actual suspend fun disconnect() {
        scope.coroutineContext[Job]?.cancelAndJoinChildren()
        connection.suspendUntilDisconnected()
        // todo: emit Event.Disconnected? or should it be emitted from Callback?
        // todo: finally gatt.disconnect()?
    }

    private suspend fun discoverServices(): Boolean = connection.request {
        discoverServices()
    }

    public suspend fun requestMtu(mtu: Int): Unit = connection.request {
        requestMtu(mtu)
    }

    public actual suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ): Unit = connection.request {
        val bluetoothGattCharacteristic = characteristic.bluetoothGattCharacteristic.also {
            it.value = data
            it.writeType = writeType.intValue
        }
        writeCharacteristic(bluetoothGattCharacteristic)
    }

    public actual suspend fun read(
        characteristic: Characteristic
    ): ByteArray = connection.request {
        readCharacteristic(characteristic.bluetoothGattCharacteristic)
    }

    public actual suspend fun write(
        descriptor: Descriptor,
        data: ByteArray
    ): Unit = connection.request {
        TODO("Not yet implemented")
    }

    public actual suspend fun read(
        descriptor: Descriptor
    ): ByteArray = connection.request {
        TODO("Not yet implemented")
    }

    public actual fun observe(
        characteristic: Characteristic
    ): Flow<ByteArray> {
        TODO("Not yet implemented")
    }

    // todo: toString
}

private val WriteType.intValue: Int
    get() = when (this) {
        WithResponse -> WRITE_TYPE_DEFAULT
        WithoutResponse -> WRITE_TYPE_NO_RESPONSE
    }

private fun <T> Flow<T>.launchIn(
    scope: CoroutineScope,
    start: CoroutineStart = CoroutineStart.DEFAULT
): Job = scope.launch(start = start) {
    collect()
}
