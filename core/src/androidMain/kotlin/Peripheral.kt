package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
import android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    androidContext: Context,
    private val bluetoothDevice: BluetoothDevice,
) : Peripheral {

    private val context = androidContext.applicationContext

    private val job = SupervisorJob(parentCoroutineContext[Job]).apply {
        invokeOnCompletion { _connection?.close() }
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

    private val connectJob = atomic<Job?>(null)

    private val _ready = MutableStateFlow(false)
    internal suspend fun suspendUntilReady() {
        combine(_ready, state) { ready, state -> ready && state == State.Connected }.first { it }
    }

    private fun createConnectJob(): Job = scope.launch(start = LAZY) {
        _ready.value = false

        val connection =
            bluetoothDevice.connect(context, _state) ?: throw ConnectionRejectedException()
        _connection = connection

        try {
            connection
                .characteristicChanges
                .onEach(observers.characteristicChanges::emit)
                .catch { cause -> if (cause !is ConnectionLostException) throw cause }
                .launchIn(scope, start = UNDISPATCHED)

            suspendUntilConnected()
            discoverServices()
            observers.rewire()
        } catch (t: Throwable) {
            connection.close()
            _connection = null
            if (t !is ConnectionLostException) throw t
        }

        println("Ready")
        _ready.value = true
    }.apply {
        // fixme: Clear on disconnect (not at end of connect).
        invokeOnCompletion { connectJob.value = null }
    }

    public override suspend fun connect() {
        check(!job.isCancelled) { "Cannot connect, scope is cancelled for $this" }
        connectJob.updateAndGet { it ?: createConnectJob() }!!.join()
    }

    public override suspend fun disconnect() {
        try {
            job.cancelAndJoinChildren()
            connection.bluetoothGatt.disconnect()
            suspendUntilDisconnected()
            connectJob.value = null
        } finally {
            _connection?.close()
            _connection = null
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
            .also(::log)
    }

    private fun log(services: List<PlatformService>) {
        services.forEach { service ->
            Log.d(TAG, "• Service: ${service.serviceUuid}")
            service.characteristics.forEach { characteristic ->
                Log.d(TAG, "    • Characteristic: ${characteristic.characteristicUuid}")
                characteristic.descriptors.forEach { descriptor ->
                    Log.d(TAG, "        • Descriptor: ${descriptor.descriptorUuid}")
                }
            }
        }
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
