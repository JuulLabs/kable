package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
import android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
import com.benasher44.uuid.uuidFrom
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
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

private val clientCharacteristicConfigUuid = uuidFrom(CLIENT_CHARACTERISTIC_CONFIG_UUID)

public actual fun CoroutineScope.peripheral(
    advertisement: Advertisement,
): Peripheral = peripheral(advertisement.bluetoothDevice)

public fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
): Peripheral = AndroidPeripheral(coroutineContext, bluetoothDevice)

public class AndroidPeripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val bluetoothDevice: BluetoothDevice,
) : Peripheral {

    private val job = SupervisorJob(parentCoroutineContext[Job]).apply {
        invokeOnCompletion { dispose() }
    }
    private val scope = CoroutineScope(parentCoroutineContext + job)

    private val _state = MutableStateFlow<State>(State.Disconnected())
    public override val state: Flow<State> = _state.asStateFlow()

    private val observers = Observers(this)

    @Volatile
    internal var platformServices: List<PlatformService>? = null
    public override val services: List<DiscoveredService>?
        get() = platformServices?.map { it.toDiscoveredService() }

    @Volatile
    private var _connection: Connection? = null
    private val connection: Connection
        inline get() = _connection ?: throw NotReadyException(toString())

    private val connectJob = atomic<Deferred<Unit>?>(null)

    private val _ready = MutableStateFlow(false)
    internal suspend fun suspendUntilReady() {
        combine(_ready, state) { ready, state -> ready && state == State.Connected }.first { it }
    }

    private fun establishConnection(): Connection =
        bluetoothDevice.connect(
            applicationContext,
            _state,
            invokeOnClose = { connectJob.value = null }
        ) ?: throw ConnectionRejectedException()

    /** Creates a connect [Job] that completes when connection is established, or failure occurs. */
    private fun connectAsync() = scope.async(start = LAZY) {
        _ready.value = false

        val connection = establishConnection().also { _connection = it }
        connection
            .characteristicChanges
            .onEach(observers.characteristicChanges::emit)
            .launchIn(scope, start = UNDISPATCHED)

        try {
            suspendUntilConnected()
            discoverServices()
            observers.rewire()
        } catch (t: Throwable) {
            dispose()
            throw t
        }

        _ready.value = true
    }

    private fun dispose() {
        _connection?.close()
        _connection = null
    }

    public override suspend fun connect() {
        check(job.isNotCancelled) { "Cannot connect, scope is cancelled for $this" }
        connectJob.updateAndGet { it ?: connectAsync() }!!.await()
    }

    public override suspend fun disconnect() {
        try {
            _connection?.apply {
                bluetoothGatt.disconnect()
                suspendUntilDisconnected()
            }
        } finally {
            dispose()
        }
    }

    public override suspend fun rssi(): Int = connection.execute<OnReadRemoteRssi> {
        readRemoteRssi()
    }.rssi

    private suspend fun discoverServices() {
        connection.execute<OnServicesDiscovered> {
            discoverServices()
        }
        platformServices = connection.bluetoothGatt
            .services
            .map { it.toPlatformService() }
    }

    public suspend fun requestMtu(mtu: Int): Unit = connection.execute {
        this@execute.requestMtu(mtu)
    }

    public override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ) {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        connection.execute<OnCharacteristicWrite> {
            bluetoothGattCharacteristic.value = data
            bluetoothGattCharacteristic.writeType = writeType.intValue
            writeCharacteristic(bluetoothGattCharacteristic)
        }
    }

    public override suspend fun read(
        characteristic: Characteristic,
    ): ByteArray {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        return connection.execute<OnCharacteristicRead> {
            readCharacteristic(bluetoothGattCharacteristic)
        }.value
    }

    public override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ) {
        val bluetoothGattDescriptor = bluetoothGattDescriptorFrom(descriptor)
        connection.execute<OnDescriptorWrite> {
            bluetoothGattDescriptor.value = data
            writeDescriptor(bluetoothGattDescriptor)
        }
    }

    public override suspend fun read(
        descriptor: Descriptor,
    ): ByteArray {
        val bluetoothGattDescriptor = bluetoothGattDescriptorFrom(descriptor)
        return connection.execute<OnDescriptorRead> {
            readDescriptor(bluetoothGattDescriptor)
        }.value
    }

    public override fun observe(
        characteristic: Characteristic,
    ): Flow<ByteArray> = observers.acquire(characteristic)

    internal suspend fun startNotifications(characteristic: Characteristic) {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        connection.bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true)

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
        connection.bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, false)
    }

    override fun toString(): String = "Peripheral(bluetoothDevice=$bluetoothDevice)"
}

private suspend fun Peripheral.suspendUntilConnected() {
    state
        .onEach { if (it is State.Disconnected) throw ConnectionLostException() }
        .first { it == State.Connected }
}

private suspend fun Peripheral.suspendUntilDisconnected() {
    state.first { it is State.Disconnected }
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
