package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
import android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
import android.content.Context
import com.benasher44.uuid.uuidFrom
import com.juul.kable.Event.Rejected
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import com.juul.kable.external.CLIENT_CHARACTERISTIC_CONFIG_UUID
import com.juul.kable.gatt.Response.OnCharacteristicRead
import com.juul.kable.gatt.Response.OnCharacteristicWrite
import com.juul.kable.gatt.Response.OnDescriptorRead
import com.juul.kable.gatt.Response.OnDescriptorWrite
import com.juul.kable.gatt.Response.OnReadRemoteRssi
import com.juul.kable.gatt.Response.OnServicesDiscovered
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.updateAndGet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

private val clientCharacteristicConfigUuid = uuidFrom(CLIENT_CHARACTERISTIC_CONFIG_UUID)

internal fun CoroutineScope.peripheral(
    androidContext: Context,
    bluetoothDevice: BluetoothDevice,
): Peripheral = AndroidPeripheral(coroutineContext, androidContext, bluetoothDevice)

public class AndroidPeripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val androidContext: Context,
    private val bluetoothDevice: BluetoothDevice,
): Peripheral {

    private val job = SupervisorJob(parentCoroutineContext[Job])
    private val scope = CoroutineScope(parentCoroutineContext + job)

    private val _state = MutableStateFlow<State>(State.Disconnected)
    public override val state: Flow<State> = _state.asStateFlow()
    private fun setState(state: State) { _state.value = state }

    private val _events = MutableSharedFlow<Event>()
    public override val events: Flow<Event> = _events.asSharedFlow()
    private suspend fun emit(event: Event) { _events.emit(event) }

    private val observers = Observers(this)

    internal val platformServices: List<PlatformService>? = null
    public override val services: List<DiscoveredService>?
        get() = platformServices?.map { it.toDiscoveredService() }

    @Volatile
    private var _connection: Connection? = null
    private val connection: Connection
        inline get() = _connection ?: throw NotReadyException(toString())

    private val connectJob = atomic<Job?>(null)

    private fun createConnectJob(): Job = scope.launch(start = LAZY) {
        setState(State.Connecting)

        val connection = bluetoothDevice.connect(androidContext, _state)
        if (connection == null) {
            emit(Rejected)
            return@launch
        }

        try {
            connection
                .characteristicChanges
                .onEach(observers.characteristicChanges::emit)
                .catch { cause -> if (cause !is ConnectionLostException) throw cause }
                .launchIn(scope, start = UNDISPATCHED)

            connection.suspendUntilConnected()
            discoverServices()
            observers.rewire()
            _connection = connection
            emit(Event.Connected(this@AndroidPeripheral))
        } catch (t: Throwable) {
            connection.close()
            _connection = null
            throw t
        }
    }

    public override suspend fun connect() {
        check(!job.isCancelled) { "Cannot connect, scope is cancelled for $this" }
        connectJob.updateAndGet { it ?: createConnectJob() }!!.join()
    }

    public override suspend fun disconnect() {
        try {
            job.cancelAndJoinChildren()
            connection.suspendUntilDisconnected()
            connectJob.value = null
        } finally {
            _connection?.close()
            _connection = null
            _state.value = State.Disconnected
        }
    }

    public override suspend fun rssi(): Int = connection.request<OnReadRemoteRssi> {
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

    public override suspend fun write(
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

    public override suspend fun read(
        characteristic: Characteristic,
    ): ByteArray {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        return connection.request<OnCharacteristicRead> {
            readCharacteristic(bluetoothGattCharacteristic)
        }.value
    }

    public override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ) {
        val bluetoothGattDescriptor = bluetoothGattDescriptorFrom(descriptor)
        connection.request<OnDescriptorWrite> {
            writeDescriptor(bluetoothGattDescriptor)
        }
    }

    public override suspend fun read(
        descriptor: Descriptor,
    ): ByteArray {
        val bluetoothGattDescriptor = bluetoothGattDescriptorFrom(descriptor)
        return connection.request<OnDescriptorRead> {
            readDescriptor(bluetoothGattDescriptor)
        }.value
    }

    public override fun observe(
        characteristic: Characteristic,
    ): Flow<ByteArray> = observers.acquire(characteristic)

    internal suspend fun startNotifications(characteristic: Characteristic) {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        connection.setNotification(bluetoothGattCharacteristic, enable = true)

        val descriptor = LazyDescriptor(
            serviceUuid = characteristic.serviceUuid,
            characteristicUuid = characteristic.characteristicUuid,
            descriptorUuid = clientCharacteristicConfigUuid
        )
        write(descriptor, ENABLE_NOTIFICATION_VALUE)
    }

    internal suspend fun stopNotifications(characteristic: Characteristic) {
        val descriptor = LazyDescriptor(
            serviceUuid = characteristic.serviceUuid,
            characteristicUuid = characteristic.characteristicUuid,
            descriptorUuid = clientCharacteristicConfigUuid
        )
        write(descriptor, DISABLE_NOTIFICATION_VALUE)

        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        connection.setNotification(bluetoothGattCharacteristic, enable = false)
    }

    override fun toString(): String = "Peripheral(bluetoothDevice=$bluetoothDevice)"
}

private fun AndroidPeripheral.bluetoothGattCharacteristicFrom(
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

private fun AndroidPeripheral.bluetoothGattDescriptorFrom(
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
