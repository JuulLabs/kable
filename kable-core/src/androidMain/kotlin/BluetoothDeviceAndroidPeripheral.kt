package com.juul.kable

import android.bluetooth.BluetoothAdapter.STATE_OFF
import android.bluetooth.BluetoothAdapter.STATE_ON
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
import com.benasher44.uuid.uuidFrom
import com.juul.kable.AndroidPeripheral.Priority
import com.juul.kable.AndroidPeripheral.Type
import com.juul.kable.State.Disconnected
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import com.juul.kable.external.CLIENT_CHARACTERISTIC_CONFIG_UUID
import com.juul.kable.gatt.Response.OnCharacteristicRead
import com.juul.kable.gatt.Response.OnCharacteristicWrite
import com.juul.kable.gatt.Response.OnDescriptorRead
import com.juul.kable.gatt.Response.OnDescriptorWrite
import com.juul.kable.gatt.Response.OnReadRemoteRssi
import com.juul.kable.gatt.Response.OnServicesDiscovered
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.logs.Logging.DataProcessor.Operation
import com.juul.kable.logs.detail
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

private val clientCharacteristicConfigUuid = uuidFrom(CLIENT_CHARACTERISTIC_CONFIG_UUID)

// Number of service discovery attempts to make if no services are discovered.
// https://github.com/JuulLabs/kable/issues/295
private const val DISCOVER_SERVICES_RETRIES = 5

internal class BluetoothDeviceAndroidPeripheral(
    parentCoroutineContext: CoroutineContext,
    private val bluetoothDevice: BluetoothDevice,
    private val autoConnectPredicate: () -> Boolean,
    private val transport: Transport,
    private val phy: Phy,
    observationExceptionHandler: ObservationExceptionHandler,
    private val onServicesDiscovered: ServicesDiscoveredAction,
    private val logging: Logging,
) : AndroidPeripheral {

    private val logger = Logger(logging, tag = "Kable/Peripheral", identifier = bluetoothDevice.address)

    private val _state = MutableStateFlow<State>(Disconnected())
    override val state: StateFlow<State> = _state.asStateFlow()

    override val identifier: String = bluetoothDevice.address

    // todo: Spin up/down w/ connection, rather than matching lifecycle of peripheral.
    private val threading = bluetoothDevice.threading()

    private val _mtu = MutableStateFlow<Int?>(null)
    override val mtu: StateFlow<Int?> = _mtu.asStateFlow()

    private val observers = Observers<ByteArray>(this, logging, exceptionHandler = observationExceptionHandler)

    @Volatile
    private var _discoveredServices: List<DiscoveredService>? = null
    private val discoveredServices: List<DiscoveredService>
        get() = _discoveredServices
            ?: throw IllegalStateException("Services have not been discovered for $this")

    override val services: List<DiscoveredService>?
        get() = _discoveredServices?.toList()

    @Volatile
    private var _connection: Connection? = null
    private val connection: Connection
        inline get() = _connection ?: throw NotReadyException(toString())

    override val name: String? get() = bluetoothDevice.name

    /**
     * It's important that we instantiate this scope as late as possible, since [dispose] will be
     * called immediately if the parent job is already complete. Doing so late in <init> is fine,
     * but early in <init> it could reference non-nullable variables that are not yet set and crash.
     */
    private val scope = CoroutineScope(
        parentCoroutineContext +
            SupervisorJob(parentCoroutineContext.job).apply { invokeOnCompletion(::dispose) } +
            CoroutineName("Kable/Peripheral/${bluetoothDevice.address}"),
    )

    private val connectAction = scope.sharedRepeatableAction(::establishConnection)

    private suspend fun establishConnection(scope: CoroutineScope) {
        checkBluetoothAdapterState(expected = STATE_ON)
        bluetoothState.watchForDisablingIn(scope)

        logger.info { message = "Connecting" }
        _state.value = State.Connecting.Bluetooth

        try {
            _connection = bluetoothDevice.connect(
                scope,
                applicationContext,
                autoConnectPredicate(),
                transport,
                phy,
                _state,
                _mtu,
                observers.characteristicChanges,
                logging,
                threading,
            ) ?: throw ConnectionRejectedException()

            suspendUntilOrThrow<State.Connecting.Services>()
            discoverServices()
            onServicesDiscovered(ServicesDiscoveredPeripheral(this@BluetoothDeviceAndroidPeripheral))

            _state.value = State.Connecting.Observes
            logger.verbose { message = "Configuring characteristic observations" }
            observers.onConnected()
        } catch (e: Exception) {
            closeConnection()
            val failure = e.unwrapCancellationCause()
            logger.error(failure) { message = "Failed to connect" }
            throw failure
        }

        logger.info { message = "Connected" }
        _state.value = State.Connected

        state.watchForConnectionLossIn(scope)
    }

    private fun Flow<Int>.watchForDisablingIn(scope: CoroutineScope): Job =
        scope.launch(start = UNDISPATCHED) {
            filter { state -> state == STATE_TURNING_OFF || state == STATE_OFF }
                .collect { state ->
                    logger.debug {
                        message = "Bluetooth disabled"
                        detail("state", state)
                    }
                    closeConnection()
                    throw BluetoothDisabledException()
                }
        }

    private fun Flow<State>.watchForConnectionLossIn(scope: CoroutineScope) =
        state
            .filter { it == State.Disconnecting || it is Disconnected }
            .onEach { state ->
                logger.debug {
                    message = "Disconnect detected"
                    detail("state", state.toString())
                }
                throw ConnectionLostException("$this $state")
            }
            .launchIn(scope)

    override val type: Type
        get() = typeFrom(bluetoothDevice.type)

    override val address: String = bluetoothDevice.address

    override suspend fun connect() {
        connectAction.await()
    }

    override suspend fun disconnect() {
        if (state.value is State.Connected) {
            // Disconnect from active connection.
            _connection?.bluetoothGatt?.disconnect()
        } else {
            // Cancel in-flight connection attempt.
            connectAction.cancelAndJoin(CancellationException(NotConnectedException()))
        }
        suspendUntil<Disconnected>()
        logger.info { message = "Disconnected" }
    }

    private fun dispose(cause: Throwable?) {
        closeConnection()
        threading.close()
        logger.info(cause) { message = "Disposed" }
    }

    private fun closeConnection() {
        _connection?.bluetoothGatt?.close()
        setDisconnected()
    }

    private fun setDisconnected() {
        // Avoid trampling existing `Disconnected` state (and its properties) by only updating if not already `Disconnected`.
        _state.update { previous -> previous as? Disconnected ?: Disconnected() }
    }

    override fun requestConnectionPriority(priority: Priority): Boolean {
        logger.debug {
            message = "requestConnectionPriority"
            detail("priority", priority.name)
        }
        return connection.bluetoothGatt
            .requestConnectionPriority(priority.intValue)
    }

    override suspend fun rssi(): Int = connection.execute<OnReadRemoteRssi> {
        readRemoteRssiOrThrow()
    }.rssi

    private suspend fun discoverServices() {
        logger.verbose { message = "discoverServices" }

        repeat(DISCOVER_SERVICES_RETRIES) { attempt ->
            connection.execute<OnServicesDiscovered> {
                discoverServicesOrThrow()
            }
            val services = withContext(connection.dispatcher) {
                connection.bluetoothGatt.services.map(::DiscoveredService)
            }

            if (services.isEmpty()) {
                logger.warn { message = "Empty services (attempt ${attempt + 1} of $DISCOVER_SERVICES_RETRIES)" }
            } else {
                logger.verbose { message = "Discovered ${services.count()} services" }
                _discoveredServices = services
                return
            }
        }
        _discoveredServices = emptyList()
    }

    override suspend fun requestMtu(mtu: Int): Int {
        logger.debug {
            message = "requestMtu"
            detail("mtu", mtu)
        }
        return connection.requestMtu(mtu)
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

        val platformCharacteristic = discoveredServices.obtain(characteristic, writeType.properties)
        connection.execute<OnCharacteristicWrite> {
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

        val platformCharacteristic = discoveredServices.obtain(characteristic, Read)
        return connection.execute<OnCharacteristicRead> {
            readCharacteristicOrThrow(platformCharacteristic)
        }.value!!
    }

    override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ) {
        write(discoveredServices.obtain(descriptor), data)
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

        connection.execute<OnDescriptorWrite> {
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

        val platformDescriptor = discoveredServices.obtain(descriptor)
        return connection.execute<OnDescriptorRead> {
            readDescriptorOrThrow(platformDescriptor)
        }.value!!
    }

    override fun observe(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<ByteArray> = observers.acquire(characteristic, onSubscription)

    internal suspend fun startObservation(characteristic: Characteristic) {
        logger.debug {
            message = "setCharacteristicNotification"
            detail(characteristic)
            detail("value", "true")
        }

        val platformCharacteristic = discoveredServices.obtain(characteristic, Notify or Indicate)
        connection
            .bluetoothGatt
            .setCharacteristicNotificationOrThrow(platformCharacteristic, true)
        setConfigDescriptor(platformCharacteristic, enable = true)
    }

    internal suspend fun stopObservation(characteristic: Characteristic) {
        val platformCharacteristic = discoveredServices.obtain(characteristic, Notify or Indicate)
        setConfigDescriptor(platformCharacteristic, enable = false)

        logger.debug {
            message = "setCharacteristicNotification"
            detail(characteristic)
            detail("value", "false")
        }
        connection
            .bluetoothGatt
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
