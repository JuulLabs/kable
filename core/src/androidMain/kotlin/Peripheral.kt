package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
import android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
import com.benasher44.uuid.uuidFrom
import com.juul.kable.WriteNotificationDescriptor.Always
import com.juul.kable.WriteNotificationDescriptor.Never
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
private val writeNotificationDescriptorDefault = Always

public actual fun CoroutineScope.peripheral(
    advertisement: Advertisement,
): Peripheral = peripheral(advertisement.bluetoothDevice)

public fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    writeObserveDescriptor: WriteNotificationDescriptor,
): Peripheral = peripheral(advertisement.bluetoothDevice, writeObserveDescriptor)

public fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    writeObserveDescriptor: WriteNotificationDescriptor = writeNotificationDescriptorDefault,
): Peripheral = AndroidPeripheral(coroutineContext, bluetoothDevice, writeObserveDescriptor)

public class AndroidPeripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val bluetoothDevice: BluetoothDevice,
    private val writeObserveDescriptor: WriteNotificationDescriptor = writeNotificationDescriptorDefault,
) : Peripheral {

    private val job = SupervisorJob(parentCoroutineContext[Job]).apply {
        invokeOnCompletion { dispose() }
    }
    private val scope = CoroutineScope(parentCoroutineContext + job)

    private val _state = MutableStateFlow<State>(State.Disconnected())
    public override val state: Flow<State> = _state.asStateFlow()

    private val observers = Observers(this)

    @Volatile
    private var _platformServices: List<PlatformService>? = null
    private val platformServices: List<PlatformService>
        get() = checkNotNull(_platformServices) { "Services have not been discovered for $this" }

    public override val services: List<DiscoveredService>?
        get() = _platformServices?.map { it.toDiscoveredService() }

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
        _platformServices = connection.bluetoothGatt
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
        write(bluetoothGattDescriptorFrom(descriptor), data)
    }

    private suspend fun write(
        bluetoothGattDescriptor: BluetoothGattDescriptor,
        data: ByteArray,
    ) {
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
        val platformCharacteristic = platformServices.findCharacteristic(characteristic)
        connection
            .bluetoothGatt
            .setCharacteristicNotification(platformCharacteristic.bluetoothGattCharacteristic, true)

        writeConfigDescriptor(platformCharacteristic, ENABLE_NOTIFICATION_VALUE)
    }

    internal suspend fun stopNotifications(characteristic: Characteristic) {
        val platformCharacteristic = platformServices.findCharacteristic(characteristic)
        writeConfigDescriptor(platformCharacteristic, DISABLE_NOTIFICATION_VALUE)

        val bluetoothGattCharacteristic = platformCharacteristic.bluetoothGattCharacteristic
        connection
            .bluetoothGatt
            .setCharacteristicNotification(bluetoothGattCharacteristic, false)
    }

    private suspend fun writeConfigDescriptor(
        characteristic: PlatformCharacteristic,
        value: ByteArray
    ) {
        if (writeObserveDescriptor == Never) return

        val bluetoothGattDescriptor = characteristic
            .descriptors
            .firstOrNull(clientCharacteristicConfigUuid)
            ?.bluetoothGattDescriptor

        if (bluetoothGattDescriptor != null) {
            write(bluetoothGattDescriptor, value)
        } else if (writeObserveDescriptor == Always) {
            error("Unable to start observation for characteristic ${characteristic.characteristicUuid}, config descriptor not found.")
        }
    }

    private fun bluetoothGattCharacteristicFrom(
        characteristic: Characteristic
    ) = platformServices.findCharacteristic(characteristic).bluetoothGattCharacteristic

    private fun bluetoothGattDescriptorFrom(
        descriptor: Descriptor
    ) = platformServices.findDescriptor(descriptor).bluetoothGattDescriptor

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

private val WriteType.intValue: Int
    get() = when (this) {
        WithResponse -> WRITE_TYPE_DEFAULT
        WithoutResponse -> WRITE_TYPE_NO_RESPONSE
    }
