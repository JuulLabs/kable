package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_INDICATE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
import android.bluetooth.BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
import android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
import android.util.Log
import com.benasher44.uuid.uuidFrom
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import com.juul.kable.external.CLIENT_CHARACTERISTIC_CONFIG_UUID
import com.juul.kable.gatt.Response.OnCharacteristicRead
import com.juul.kable.gatt.Response.OnCharacteristicWrite
import com.juul.kable.gatt.Response.OnDescriptorRead
import com.juul.kable.gatt.Response.OnDescriptorWrite
import com.juul.kable.gatt.Response.OnMtuChanged
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
import kotlin.DeprecationLevel.WARNING
import kotlin.coroutines.CoroutineContext

private val clientCharacteristicConfigUuid = uuidFrom(CLIENT_CHARACTERISTIC_CONFIG_UUID)

/** Preferred transport for GATT connections to remote dual-mode devices. */
public enum class Transport {

    /** No preference of physical transport for GATT connections to remote dual-mode devices. */
    Auto,

    /** Prefer BR/EDR transport for GATT connections to remote dual-mode devices. */
    BrEdr,

    /** Prefer LE transport for GATT connections to remote dual-mode devices. */
    Le,
}

/** Preferred Physical Layer (PHY) for connections to remote LE devices. */
public enum class Phy {

    /** Bluetooth LE 1M PHY. */
    Le1M,

    /**
     * Bluetooth LE 2M PHY.
     *
     * Per [Exploring Bluetooth 5 – Going the Distance](https://www.bluetooth.com/blog/exploring-bluetooth-5-going-the-distance/#mcetoc_1d7vdh6b25):
     * "The new LE 2M PHY allows the physical layer to operate at 2 Ms/s and thus enables higher data rates than LE 1M
     * and Bluetooth 4."
     */
    Le2M,

    /**
     * Bluetooth LE Coded PHY.
     *
     * Per [Exploring Bluetooth 5 – Going the Distance](https://www.bluetooth.com/blog/exploring-bluetooth-5-going-the-distance/#mcetoc_1d7vdh6b26):
     * "The LE Coded PHY allows range to be quadrupled (approximately), compared to Bluetooth® 4 and this has been
     * accomplished without increasing the transmission power required."
     */
    LeCoded,
}

public actual fun CoroutineScope.peripheral(
    advertisement: Advertisement,
): Peripheral = peripheral(advertisement.bluetoothDevice)

/**
 * @param transport preferred transport for GATT connections to remote dual-mode devices.
 * @param phy preferred PHY for connections to remote LE device.
 */
public fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    transport: Transport = Transport.Le,
    phy: Phy = Phy.Le1M,
): Peripheral = peripheral(advertisement.bluetoothDevice, transport, phy)

/**
 * @param transport preferred transport for GATT connections to remote dual-mode devices.
 * @param phy preferred PHY for connections to remote LE device.
 */
public fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    transport: Transport = Transport.Le,
    phy: Phy = Phy.Le1M,
): Peripheral = AndroidPeripheral(coroutineContext, bluetoothDevice, transport, phy)

@Deprecated(
    message = "'writeObserveDescriptor' parameter is no longer used and is handled automatically by 'observe' function. 'writeObserveDescriptor' argument will be removed in a future release.",
    replaceWith = ReplaceWith("peripheral(advertisement)"),
    level = WARNING,
)
public fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    writeObserveDescriptor: WriteNotificationDescriptor,
): Peripheral = peripheral(advertisement.bluetoothDevice, writeObserveDescriptor)

@Deprecated(
    message = "'writeObserveDescriptor' parameter is no longer used and is handled automatically by 'observe' function. 'writeObserveDescriptor' argument will be removed in a future release.",
    replaceWith = ReplaceWith("peripheral(advertisement)"),
    level = WARNING,
)
public fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    writeObserveDescriptor: WriteNotificationDescriptor,
): Peripheral = AndroidPeripheral(coroutineContext, bluetoothDevice, Transport.Le, Phy.Le1M)

public class AndroidPeripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val bluetoothDevice: BluetoothDevice,
    private val transport: Transport,
    private val phy: Phy,
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
            transport,
            phy,
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
            Log.d(TAG, "Connecting to ${connection.bluetoothGatt.device}")
            suspendUntilConnected()
            Log.d(TAG, "Discovering services for ${connection.bluetoothGatt.device}")
            discoverServices()
            Log.d(TAG, "Rewiring observers for ${connection.bluetoothGatt.device}")
            observers.rewire()
            Log.d(TAG, "Connected to ${connection.bluetoothGatt.device}")
        } catch (t: Throwable) {
            dispose()
            throw t
        }

        _ready.value = true
    }

    private fun dispose() {
        Log.d(TAG, "Disposing of ${_connection?.bluetoothGatt?.device}")
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

    public suspend fun requestMtu(mtu: Int) {
        connection.execute<OnMtuChanged> {
            this@execute.requestMtu(mtu)
        }
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
            Log.d(TAG, "Writing ${data.toHexString()} to characteristic ${bluetoothGattCharacteristic.uuid}")
            writeCharacteristic(bluetoothGattCharacteristic)
        }
        Log.d(TAG, "Write to characteristic ${bluetoothGattCharacteristic.uuid} complete")
    }

    public override suspend fun read(
        characteristic: Characteristic,
    ): ByteArray {
        val bluetoothGattCharacteristic = bluetoothGattCharacteristicFrom(characteristic)
        return connection.execute<OnCharacteristicRead> {
            readCharacteristic(bluetoothGattCharacteristic)
        }.value!!
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
            Log.d(TAG, "Writing ${data.toHexString()} bytes to descriptor ${bluetoothGattDescriptor.uuid}")
            writeDescriptor(bluetoothGattDescriptor)
        }
        Log.d(TAG, "Write to descriptor ${bluetoothGattDescriptor.uuid} complete")
    }

    public override suspend fun read(
        descriptor: Descriptor,
    ): ByteArray {
        val bluetoothGattDescriptor = bluetoothGattDescriptorFrom(descriptor)
        return connection.execute<OnDescriptorRead> {
            readDescriptor(bluetoothGattDescriptor)
        }.value!!
    }

    public override fun observe(
        characteristic: Characteristic,
    ): Flow<ByteArray> = observers.acquire(characteristic)

    internal suspend fun startObservation(characteristic: Characteristic) {
        val platformCharacteristic = platformServices.findCharacteristic(characteristic)
        Log.d(TAG, "setCharacteristicNotification(true) for $characteristic")
        connection
            .bluetoothGatt
            .setCharacteristicNotification(platformCharacteristic, true)
        setConfigDescriptor(platformCharacteristic, enable = true)
    }

    internal suspend fun stopObservation(characteristic: Characteristic) {
        val platformCharacteristic = platformServices.findCharacteristic(characteristic)
        setConfigDescriptor(platformCharacteristic, enable = false)
        Log.d(TAG, "setCharacteristicNotification(false) for $characteristic")
        connection
            .bluetoothGatt
            .setCharacteristicNotification(platformCharacteristic, false)
    }

    private suspend fun setConfigDescriptor(
        characteristic: PlatformCharacteristic,
        enable: Boolean,
    ) {
        val configDescriptor = characteristic.configDescriptor
        if (configDescriptor != null) {
            val bluetoothGattDescriptor = configDescriptor.bluetoothGattDescriptor

            if (enable) {
                if (characteristic.supportsNotify)
                    write(bluetoothGattDescriptor, ENABLE_NOTIFICATION_VALUE).also {
                        Log.d(TAG, "Wrote ENABLE_NOTIFICATION_VALUE to config $configDescriptor")
                    }
                else Log.d(TAG, "$characteristic does not support Notify")

                if (characteristic.supportsIndicate)
                    write(bluetoothGattDescriptor, ENABLE_INDICATION_VALUE).also {
                        Log.d(TAG, "Wrote ENABLE_INDICATION_VALUE to config $configDescriptor")
                    }
                else Log.d(TAG, "$characteristic does not support Indicate")
            } else {
                if (characteristic.supportsNotify || characteristic.supportsIndicate)
                    write(bluetoothGattDescriptor, DISABLE_NOTIFICATION_VALUE).also {
                        Log.d(TAG, "Wrote DISABLE_NOTIFICATION_VALUE to config $configDescriptor")
                    }
            }
        } else {
            Log.w(TAG, "Characteristic ${characteristic.characteristicUuid} is missing config descriptor.")
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

internal fun ByteArray.toHexString(
    separator: String? = null,
    prefix: String? = null,
    lowerCase: Boolean = false
): String {
    if (size == 0) return ""
    val hexCode = if (lowerCase) "0123456789abcdef" else "0123456789ABCDEF"
    val capacity = size * (2 + (prefix?.length ?: 0)) + (size - 1) * (separator?.length ?: 0)
    val r = StringBuilder(capacity)
    for (b in this) {
        if (separator != null && r.isNotEmpty()) r.append(separator)
        if (prefix != null) r.append(prefix)
        r.append(hexCode[b.toInt() shr 4 and 0xF])
        r.append(hexCode[b.toInt() and 0xF])
    }
    return r.toString()
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

private fun BluetoothGatt.setCharacteristicNotification(
    characteristic: PlatformCharacteristic,
    enable: Boolean,
) = setCharacteristicNotification(characteristic.bluetoothGattCharacteristic, enable)

private val PlatformCharacteristic.configDescriptor: PlatformDescriptor?
    get() = descriptors.firstOrNull(clientCharacteristicConfigUuid)

private val PlatformCharacteristic.supportsNotify: Boolean
    get() = bluetoothGattCharacteristic.properties and PROPERTY_NOTIFY != 0

private val PlatformCharacteristic.supportsIndicate: Boolean
    get() = bluetoothGattCharacteristic.properties and PROPERTY_INDICATE != 0
