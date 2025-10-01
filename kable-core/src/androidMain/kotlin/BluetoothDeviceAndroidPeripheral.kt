package com.juul.kable

import android.bluetooth.BluetoothAdapter.STATE_OFF
import android.bluetooth.BluetoothAdapter.STATE_TURNING_OFF
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC
import android.bluetooth.BluetoothDevice.DEVICE_TYPE_DUAL
import android.bluetooth.BluetoothDevice.DEVICE_TYPE_LE
import android.bluetooth.BluetoothDevice.DEVICE_TYPE_UNKNOWN
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_INDICATE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
import android.bluetooth.BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
import android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
import com.juul.kable.AndroidPeripheral.Priority
import com.juul.kable.AndroidPeripheral.Type
import com.juul.kable.State.Disconnected
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import com.juul.kable.bluetooth.checkBluetoothIsOn
import com.juul.kable.bluetooth.checkBluetoothIsSupported
import com.juul.kable.bluetooth.clientCharacteristicConfigUuid
import com.juul.kable.bluetooth.requireNonZeroAddress
import com.juul.kable.gatt.Response.OnCharacteristicRead
import com.juul.kable.gatt.Response.OnCharacteristicWrite
import com.juul.kable.gatt.Response.OnDescriptorRead
import com.juul.kable.gatt.Response.OnDescriptorWrite
import com.juul.kable.gatt.Response.OnReadRemoteRssi
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.logs.Logging.DataProcessor.Operation
import com.juul.kable.logs.detail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration

// Number of service discovery attempts to make if no services are discovered.
// https://github.com/JuulLabs/kable/issues/295
private const val DISCOVER_SERVICES_RETRIES = 5

private const val DEFAULT_ATT_MTU = 23
private const val ATT_MTU_HEADER_SIZE = 3

@OptIn(KableInternalApi::class)
internal class BluetoothDeviceAndroidPeripheral(
    override val bluetoothDevice: BluetoothDevice,
    private val autoConnectPredicate: () -> Boolean,
    private val transport: Transport,
    private val phy: Phy,
    private val threadingStrategy: ThreadingStrategy,
    observationExceptionHandler: ObservationExceptionHandler,
    private val onServicesDiscovered: ServicesDiscoveredAction,
    private val logging: Logging,
    private val disconnectTimeout: Duration,
) : BasePeripheral(bluetoothDevice.toString()), AndroidPeripheral {

    init {
        onBluetoothDisabled { state ->
            logger.debug {
                message = "Bluetooth disabled"
                detail("state", state)
            }
            disconnect()
        }
    }

    private val connectAction = scope.sharedRepeatableAction(::establishConnection)

    override val identifier: String = bluetoothDevice.address
    private val logger = Logger(logging, "Kable/Peripheral", bluetoothDevice.toString())

    private val _state = MutableStateFlow<State>(Disconnected())
    override val state = _state.asStateFlow()

    private val _services = MutableStateFlow<List<PlatformDiscoveredService>?>(null)
    override val services = _services.asStateFlow()
    private fun servicesOrThrow() = services.value ?: error("Services have not been discovered")

    private val _mtu = MutableStateFlow<Int?>(null)
    override val mtu = _mtu.asStateFlow()

    private val observers = Observers<ByteArray>(this, logging, false, observationExceptionHandler)

    private val connection = MutableStateFlow<Connection?>(null)
    private fun connectionOrThrow() =
        connection.value
            ?: throw NotConnectedException("Connection not established, current state: ${state.value}")

    override val type: Type
        get() = typeFrom(bluetoothDevice.type)

    override val address: String = requireNonZeroAddress(bluetoothDevice.address)

    @ExperimentalApi
    override val name: String?
        get() = bluetoothDevice.name

    private suspend fun establishConnection(scope: CoroutineScope): CoroutineScope {
        checkBluetoothIsSupported()
        checkBluetoothIsOn()

        logger.info { message = "Connecting" }
        _state.value = State.Connecting.Bluetooth

        try {
            connection.value = bluetoothDevice.connect(
                scope.coroutineContext,
                applicationContext,
                autoConnectPredicate(),
                transport,
                phy,
                _state,
                _services,
                _mtu,
                observers.characteristicChanges,
                logging,
                threadingStrategy,
                disconnectTimeout,
            )

            suspendUntil<State.Connecting.Services>()
            discoverServices()
            configureCharacteristicObservations()
        } catch (e: Exception) {
            val failure = e.unwrapCancellationException()
            logger.error(failure) { message = "Failed to establish connection" }
            throw failure
        }

        val connectionScope = connectionOrThrow().taskScope
        logger.info { message = "Connected" }
        _state.value = State.Connected(connectionScope)

        return connectionScope
    }

    private suspend fun configureCharacteristicObservations() {
        logger.verbose { message = "Configuring characteristic observations" }
        _state.value = State.Connecting.Observes
        observers.onConnected()
    }

    override suspend fun connect(): CoroutineScope =
        connectAction.awaitConnect()

    override suspend fun disconnect() {
        connectAction.cancelAndJoin(
            CancellationException(NotConnectedException("Disconnect requested")),
        )
    }

    override fun requestConnectionPriority(priority: Priority): Boolean {
        logger.debug {
            message = "requestConnectionPriority"
            detail("priority", priority.name)
        }
        return connectionOrThrow()
            .gatt
            .requestConnectionPriority(priority.intValue)
    }

    override suspend fun maximumWriteValueLengthForType(writeType: WriteType): Int =
        (mtu.value ?: DEFAULT_ATT_MTU) - ATT_MTU_HEADER_SIZE

    @ExperimentalApi // Experimental until Web Bluetooth advertisements APIs are stable.
    override suspend fun rssi(): Int =
        connectionOrThrow().execute<OnReadRemoteRssi> {
            readRemoteRssiOrThrow()
        }.rssi

    private suspend fun discoverServices() {
        connectionOrThrow().discoverServices(retries = DISCOVER_SERVICES_RETRIES)
        unwrapCancellationExceptions {
            onServicesDiscovered(ServicesDiscoveredPeripheral(this))
        }
    }

    override suspend fun requestMtu(mtu: Int): Int {
        logger.debug {
            message = "requestMtu"
            detail("mtu", mtu)
        }
        return connectionOrThrow().requestMtu(mtu)
    }

    override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ) {
        logger.debug {
            message = "write"
            detail(characteristic)
            detail(writeType)
            detail(data, Operation.Write)
        }

        val platformCharacteristic = servicesOrThrow().obtain(characteristic, writeType.properties)
        connectionOrThrow().execute<OnCharacteristicWrite> {
            writeCharacteristicOrThrow(platformCharacteristic, data, writeType.intValue)
        }
    }

    override suspend fun read(
        characteristic: Characteristic,
    ): ByteArray {
        logger.debug {
            message = "read"
            detail(characteristic)
        }

        val platformCharacteristic = servicesOrThrow().obtain(characteristic, Read)
        return connectionOrThrow().execute<OnCharacteristicRead> {
            readCharacteristicOrThrow(platformCharacteristic)
        }.value!!
    }

    override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ) {
        write(servicesOrThrow().obtain(descriptor), data)
    }

    private suspend fun write(
        platformDescriptor: PlatformDescriptor,
        data: ByteArray,
    ) {
        logger.debug {
            message = "write"
            detail(platformDescriptor)
            detail(data, Operation.Write)
        }

        connectionOrThrow().execute<OnDescriptorWrite> {
            writeDescriptorOrThrow(platformDescriptor, data)
        }
    }

    override suspend fun read(
        descriptor: Descriptor,
    ): ByteArray {
        logger.debug {
            message = "read"
            detail(descriptor)
        }

        val platformDescriptor = servicesOrThrow().obtain(descriptor)
        return connectionOrThrow().execute<OnDescriptorRead> {
            readDescriptorOrThrow(platformDescriptor)
        }.value!!
    }

    override fun observe(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<ByteArray> = observers.acquire(characteristic, onSubscription)

    internal suspend fun startObservation(characteristic: Characteristic) {
        logger.debug {
            message = "Starting observation"
            detail(characteristic)
        }

        val platformCharacteristic = servicesOrThrow().obtain(characteristic, Notify or Indicate)

        logger.verbose {
            message = "setCharacteristicNotification"
            detail(characteristic)
            detail("value", "true")
        }
        connectionOrThrow()
            .gatt
            .setCharacteristicNotificationOrThrow(platformCharacteristic, true)
        setConfigDescriptor(platformCharacteristic, enable = true)
    }

    internal suspend fun stopObservation(characteristic: Characteristic) {
        logger.debug {
            message = "Stopping observation"
            detail(characteristic)
        }

        val platformCharacteristic = servicesOrThrow().obtain(characteristic, Notify or Indicate)
        setConfigDescriptor(platformCharacteristic, enable = false)

        logger.verbose {
            message = "setCharacteristicNotification"
            detail(characteristic)
            detail("value", "false")
        }
        connectionOrThrow()
            .gatt
            .setCharacteristicNotificationOrThrow(platformCharacteristic, false)
    }

    private suspend fun setConfigDescriptor(
        characteristic: PlatformCharacteristic,
        enable: Boolean,
    ) {
        val configDescriptor = characteristic.configDescriptor
        if (configDescriptor != null) {
            if (enable) {
                when {
                    characteristic.supportsNotify -> {
                        logger.verbose {
                            message = "Writing ENABLE_NOTIFICATION_VALUE to CCCD"
                            detail(configDescriptor)
                        }
                        write(configDescriptor, ENABLE_NOTIFICATION_VALUE)
                    }
                    characteristic.supportsIndicate -> {
                        logger.verbose {
                            message = "Writing ENABLE_INDICATION_VALUE to CCCD"
                            detail(configDescriptor)
                        }
                        write(configDescriptor, ENABLE_INDICATION_VALUE)
                    }
                    else -> logger.warn {
                        message = "Characteristic supports neither notification nor indication"
                        detail(characteristic)
                    }
                }
            } else {
                if (characteristic.supportsNotify || characteristic.supportsIndicate) {
                    logger.verbose {
                        message = "Writing DISABLE_NOTIFICATION_VALUE to CCCD"
                        detail(configDescriptor)
                    }
                    write(configDescriptor, DISABLE_NOTIFICATION_VALUE)
                }
            }
        } else {
            logger.warn {
                message = "Characteristic is missing config descriptor."
                detail(characteristic)
            }
        }
    }

    private fun onBluetoothDisabled(action: suspend (bluetoothState: Int) -> Unit) {
        bluetoothState
            .filter { state -> state == STATE_TURNING_OFF || state == STATE_OFF }
            .onEach(action)
            .launchIn(scope)
    }

    override fun close() {
        scope.cancel("$this closed")
    }

    override fun toString(): String = "Peripheral(bluetoothDevice=$bluetoothDevice)"
}

private val WriteType.intValue: Int
    get() = when (this) {
        WithResponse -> WRITE_TYPE_DEFAULT
        WithoutResponse -> WRITE_TYPE_NO_RESPONSE
    }

private val Priority.intValue: Int
    get() = when (this) {
        Priority.Low -> BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER
        Priority.Balanced -> BluetoothGatt.CONNECTION_PRIORITY_BALANCED
        Priority.High -> BluetoothGatt.CONNECTION_PRIORITY_HIGH
    }

private val PlatformCharacteristic.configDescriptor: PlatformDescriptor?
    get() = descriptors.firstOrNull { clientCharacteristicConfigUuid == it.uuid }

private val PlatformCharacteristic.supportsNotify: Boolean
    get() = properties and PROPERTY_NOTIFY != 0

private val PlatformCharacteristic.supportsIndicate: Boolean
    get() = properties and PROPERTY_INDICATE != 0

private fun typeFrom(value: Int): Type = when (value) {
    DEVICE_TYPE_UNKNOWN -> Type.Unknown
    DEVICE_TYPE_CLASSIC -> Type.Classic
    DEVICE_TYPE_DUAL -> Type.DualMode
    DEVICE_TYPE_LE -> Type.LowEnergy
    else -> error("Unsupported device type: $value")
}
