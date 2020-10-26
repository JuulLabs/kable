package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import com.juul.kable.Event.Rejected
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import com.juul.kable.gatt.ConnectionLostException
import com.juul.kable.gatt.Response.OnCharacteristicRead
import com.juul.kable.gatt.Response.OnCharacteristicWrite
import com.juul.kable.gatt.Response.OnDescriptorRead
import com.juul.kable.gatt.Response.OnDescriptorWrite
import com.juul.kable.gatt.Response.OnReadRemoteRssi
import com.juul.kable.gatt.Response.OnServicesDiscovered
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.CoroutineContext

public class NotReadyException internal constructor(
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

    internal val platformServices: List<PlatformService>? = null
    public actual val services: List<DiscoveredService>?
        get() = platformServices?.map { it.toDiscoveredService() }

    public actual suspend fun connect() {
        // todo: Prevent multiple simultaneous connection attempts.

        val connection = bluetoothDevice.connect(androidContext)
        if (connection == null) {
            _events.emit(Rejected)
            return
        }

        connection.callback
            .onCharacteristicChanged
            // todo: Map to `Uuid` type in changed object (i.e. map to `Characteristic` rather than Android characteristic type).
            .onEach(observers.characteristicChanges::emit)
            .catch { cause -> if (cause !is ConnectionLostException) throw cause }
            .launchIn(scope, start = UNDISPATCHED)

        connection.suspendUntilConnected()
        discoverServices()
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

    public actual suspend fun rssi(): Int = connection.request<OnReadRemoteRssi> {
        readRemoteRssi()
    }.rssi

    private suspend fun discoverServices() {
        connection.request<OnServicesDiscovered> {
            discoverServices()
        }
        // todo: map services
//        platformServices = ...
    }

    public suspend fun requestMtu(mtu: Int): Unit = connection.request {
        requestMtu(mtu)
    }

    public actual suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ) {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        connection.request<OnCharacteristicWrite> {
            bluetoothGattCharacteristic.value = data
            bluetoothGattCharacteristic.writeType = writeType.intValue
            writeCharacteristic(bluetoothGattCharacteristic)
        }
    }

    public actual suspend fun read(
        characteristic: Characteristic,
    ): ByteArray {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        return connection.request<OnCharacteristicRead> {
            readCharacteristic(bluetoothGattCharacteristic)
        }.value
    }

    public actual suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ) {
        val bluetoothGattDescriptor = bluetoothGattDescriptorFrom(descriptor)
        connection.request<OnDescriptorWrite> {
            writeDescriptor(bluetoothGattDescriptor)
        }
    }

    public actual suspend fun read(
        descriptor: Descriptor,
    ): ByteArray {
        val bluetoothGattDescriptor = bluetoothGattDescriptorFrom(descriptor)
        return connection.request<OnDescriptorRead> {
            readDescriptor(bluetoothGattDescriptor)
        }.value
    }

    public actual fun observe(
        characteristic: Characteristic,
    ): Flow<ByteArray> = observers.acquire(characteristic)

    internal suspend fun startNotifications(characteristic: Characteristic) {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        TODO()
        // todo: set notifications to true
        // todo: write descriptor
    }

    internal suspend fun stopNotifications(characteristic: Characteristic) {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        TODO()
        // todo: write descriptor
        // todo: set notifications to false
    }

    override fun toString(): String = "Peripheral(bluetoothDevice=$bluetoothDevice)"
}

private fun Peripheral.bluetoothGattCharacteristicFrom(
    characteristic: Characteristic,
): BluetoothGattCharacteristic {
    val services = checkNotNull(platformServices) {
        "Services have not been discovered for $this"
    }

    val characteristics = services
        .first { characteristic.serviceUuid == it.serviceUuid }
        .characteristics
    return characteristics
        .first { characteristic.characteristicUuid == it.characteristicUuid }
        .bluetoothGattCharacteristic
}

private fun Peripheral.bluetoothGattDescriptorFrom(
    descriptor: Descriptor,
): BluetoothGattDescriptor {
    val services = checkNotNull(platformServices) {
        "Services have not been discovered for $this"
    }

    val characteristics = services
        .first { descriptor.serviceUuid == it.serviceUuid }
        .characteristics
    val descriptors = characteristics
        .first { descriptor.characteristicUuid == it.characteristicUuid }
        .descriptors
    return descriptors
        .first { descriptor.descriptorUuid == it.descriptorUuid }
        .bluetoothGattDescriptor
}

private val WriteType.intValue: Int
    get() = when (this) {
        WithResponse -> WRITE_TYPE_DEFAULT
        WithoutResponse -> WRITE_TYPE_NO_RESPONSE
    }

private fun <T> Flow<T>.launchIn(
    scope: CoroutineScope,
    start: CoroutineStart = CoroutineStart.DEFAULT,
): Job = scope.launch(start = start) {
    collect()
}
