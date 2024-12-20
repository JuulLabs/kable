package com.juul.kable

import com.juul.kable.CentralManagerDelegate.ConnectionEvent
import com.juul.kable.Endianness.LittleEndian
import com.juul.kable.PeripheralDelegate.Response.DidReadRssi
import com.juul.kable.PeripheralDelegate.Response.DidUpdateNotificationStateForCharacteristic
import com.juul.kable.PeripheralDelegate.Response.DidUpdateValueForDescriptor
import com.juul.kable.PeripheralDelegate.Response.DidWriteValueForCharacteristic
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import com.juul.kable.bluetooth.checkBluetoothIsOn
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.logs.Logging.DataProcessor.Operation.Write
import com.juul.kable.logs.detail
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import platform.CoreBluetooth.CBCharacteristicWriteWithResponse
import platform.CoreBluetooth.CBCharacteristicWriteWithoutResponse
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBManagerState
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBUUIDCharacteristicExtendedPropertiesString
import platform.CoreBluetooth.CBUUIDClientCharacteristicConfigurationString
import platform.CoreBluetooth.CBUUIDL2CAPPSMCharacteristicString
import platform.CoreBluetooth.CBUUIDServerCharacteristicConfigurationString
import platform.Foundation.NSData
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding
import platform.darwin.UInt16
import kotlin.time.Duration
import platform.CoreBluetooth.CBCharacteristicWriteWithResponse as CBWithResponse
import platform.CoreBluetooth.CBCharacteristicWriteWithoutResponse as CBWithoutResponse

internal class CBPeripheralCoreBluetoothPeripheral(
    private val cbPeripheral: CBPeripheral,
    observationExceptionHandler: ObservationExceptionHandler,
    private val onServicesDiscovered: ServicesDiscoveredAction,
    private val logging: Logging,
    private val disconnectTimeout: Duration,
) : BasePeripheral(cbPeripheral.identifier.toUuid()), CoreBluetoothPeripheral {

    private val central = CentralManager.Default

    override val identifier: Identifier = cbPeripheral.identifier.toUuid()
    private val logger = Logger(logging, identifier = identifier.toString())

    private val _state = MutableStateFlow<State>(State.Disconnected())
    override val state: StateFlow<State> = _state.asStateFlow()

    init {
        onStateChanged { state ->
            _state.value = state
            logger.debug {
                message = "CentralManagerDelegate state change"
                detail("state", state.toString())
            }
        }

        onBluetoothPoweredOff { state ->
            logger.info {
                message = "Bluetooth powered off"
                detail("state", state)
            }
            disconnect()
        }
    }

    private val connectAction = sharedRepeatableAction(::establishConnection)

    private val observers = Observers<NSData>(this, logging, exceptionHandler = observationExceptionHandler)
    private val canSendWriteWithoutResponse = MutableStateFlow(cbPeripheral.canSendWriteWithoutResponse)

    private val _services = MutableStateFlow<List<DiscoveredService>?>(null)
    override val services = _services.asStateFlow()
    private fun servicesOrThrow() = services.value ?: error("Services have not been discovered")

    private val connection = MutableStateFlow<Connection?>(null)
    private fun connectionOrThrow() =
        connection.value
            ?: throw NotConnectedException("Connection not established, current state: ${state.value}")

    @ExperimentalApi
    override val name: String?
        get() = cbPeripheral.name

    private suspend fun establishConnection(scope: CoroutineScope): CoroutineScope {
        central.checkBluetoothIsOn()

        logger.info { message = "Connecting" }
        _state.value = State.Connecting.Bluetooth

        try {
            connection.value = central.connectPeripheral(
                scope.coroutineContext,
                cbPeripheral,
                createPeripheralDelegate(),
                _state,
                _services,
                disconnectTimeout,
                logging,
            )
            suspendUntil<State.Connecting.Services>()
            discoverServices()
            configureCharacteristicObservations()
        } catch (e: Exception) {
            val failure = e.unwrapCancellationException()
            logger.error(failure) { message = "Failed to establish connection" }
            throw failure
        }

        logger.info { message = "Connected" }
        _state.value = State.Connected

        return connectionOrThrow().taskScope
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

    override suspend fun maximumWriteValueLengthForType(writeType: WriteType): Int {
        val type = when (writeType) {
            WithResponse -> CBCharacteristicWriteWithResponse
            WithoutResponse -> CBCharacteristicWriteWithoutResponse
        }
        return cbPeripheral.maximumWriteValueLengthForType(type).toInt()
    }

    @ExperimentalApi // Experimental until Web Bluetooth advertisements APIs are stable.
    @Throws(CancellationException::class, IOException::class)
    override suspend fun rssi(): Int = connectionOrThrow().execute<DidReadRssi> {
        cbPeripheral.readRSSI()
    }.rssi.intValue

    private suspend fun discoverServices() {
        connectionOrThrow().discoverServices()
        unwrapCancellationExceptions {
            onServicesDiscovered(ServicesDiscoveredPeripheral(this))
        }
    }

    @Throws(CancellationException::class, IOException::class)
    override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ): Unit = write(characteristic, data.toNSData(), writeType)

    @Throws(CancellationException::class, IOException::class)
    override suspend fun write(
        characteristic: Characteristic,
        data: NSData,
        writeType: WriteType,
    ) {
        logger.debug {
            message = "write"
            detail(characteristic)
            detail(writeType)
            detail(data, Write)
        }

        val platformCharacteristic = servicesOrThrow().obtain(characteristic, writeType.properties)
        when (writeType) {
            WithResponse -> connectionOrThrow().execute<DidWriteValueForCharacteristic> {
                cbPeripheral.writeValue(data, platformCharacteristic, CBWithResponse)
            }
            WithoutResponse -> connectionOrThrow().guard.withLock {
                if (!canSendWriteWithoutResponse.updateAndGet { cbPeripheral.canSendWriteWithoutResponse }) {
                    canSendWriteWithoutResponse.first { it }
                }
                central.writeValue(cbPeripheral, data, platformCharacteristic, CBWithoutResponse)
            }
        }
    }

    @Throws(CancellationException::class, IOException::class)
    override suspend fun read(
        characteristic: Characteristic,
    ): ByteArray = readAsNSData(characteristic).toByteArray()

    @Throws(CancellationException::class, IOException::class)
    override suspend fun readAsNSData(
        characteristic: Characteristic,
    ): NSData {
        logger.debug {
            message = "read"
            detail(characteristic)
        }

        val platformCharacteristic = servicesOrThrow().obtain(characteristic, Read)

        val event = connectionOrThrow().guard.withLock {
            observers
                .characteristicChanges
                .onSubscription { central.readValue(cbPeripheral, platformCharacteristic) }
                .first { event -> event.isAssociatedWith(characteristic) }
        }

        return when (event) {
            is ObservationEvent.CharacteristicChange -> event.data
            is ObservationEvent.Error -> throw IOException(cause = event.cause)
            ObservationEvent.Disconnected -> throw NotConnectedException()
        }
    }

    @Throws(CancellationException::class, IOException::class)
    override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ): Unit = write(descriptor, data.toNSData())

    @Throws(CancellationException::class, IOException::class)
    override suspend fun write(
        descriptor: Descriptor,
        data: NSData,
    ) {
        logger.debug {
            message = "write"
            detail(descriptor)
            detail(data, Write)
        }

        val platformDescriptor = servicesOrThrow().obtain(descriptor)
        connectionOrThrow().execute<DidUpdateValueForDescriptor> {
            writeValue(data, platformDescriptor)
        }
    }

    @Throws(CancellationException::class, IOException::class)
    override suspend fun read(
        descriptor: Descriptor,
    ): ByteArray = readAsNSData(descriptor).toByteArray()

    @Throws(CancellationException::class, IOException::class)
    override suspend fun readAsNSData(
        descriptor: Descriptor,
    ): NSData {
        logger.debug {
            message = "read"
            detail(descriptor)
        }

        val platformDescriptor = servicesOrThrow().obtain(descriptor)
        val updatedDescriptor = connectionOrThrow().execute<DidUpdateValueForDescriptor> {
            readValueForDescriptor(platformDescriptor)
        }.descriptor

        return when (val value = updatedDescriptor.value) {
            is NSData -> value

            is NSString -> value.dataUsingEncoding(NSUTF8StringEncoding)
                ?: byteArrayOf().toNSData().also {
                    logger.warn {
                        message = "Failed to decode descriptor"
                        detail(descriptor)
                        detail("type", "NSString")
                    }
                }

            is NSNumber -> when (updatedDescriptor.isUnsignedShortValue) {
                true -> value.unsignedShortValue.toByteArray(LittleEndian)
                false -> value.unsignedLongValue.toByteArray(LittleEndian)
            }.toNSData()

            // This case handles if CBUUIDL2CAPPSMCharacteristicString is `UInt16`, as it is unclear
            // in the Core Bluetooth documentation. See https://github.com/JuulLabs/kable/pull/706#discussion_r1680615969
            // for related discussion.
            is UInt16 -> value.toByteArray(LittleEndian).toNSData()

            else -> byteArrayOf().toNSData().also {
                logger.warn {
                    message = "Unknown descriptor type"
                    detail(descriptor)
                    value.type?.let { detail("type", it) }
                }
            }
        }
    }

    override fun observe(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<ByteArray> = observeAsNSData(characteristic, onSubscription).map(NSData::toByteArray)

    override fun observeAsNSData(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<NSData> = observers.acquire(characteristic, onSubscription)

    internal suspend fun startNotifications(characteristic: Characteristic) {
        logger.debug {
            message = "CentralManager.notify"
            detail(characteristic)
        }

        val platformCharacteristic = servicesOrThrow().obtain(characteristic, Notify or Indicate)
        connectionOrThrow().execute<DidUpdateNotificationStateForCharacteristic> {
            setNotifyValue(true, platformCharacteristic)
        }
    }

    internal suspend fun stopNotifications(characteristic: Characteristic) {
        logger.debug {
            message = "CentralManager.cancelNotify"
            detail(characteristic)
        }

        val platformCharacteristic = servicesOrThrow().obtain(characteristic, Notify or Indicate)
        connectionOrThrow().execute<DidUpdateNotificationStateForCharacteristic> {
            setNotifyValue(false, platformCharacteristic)
        }
    }

    private fun onStateChanged(action: (State) -> Unit) {
        central.delegate
            .connectionEvents
            .filter { event -> event.identifier == cbPeripheral.identifier }
            .map(ConnectionEvent::toState)
            .onEach(action)
            .launchIn(this)
    }

    private fun onBluetoothPoweredOff(action: suspend (CBManagerState) -> Unit) {
        central.delegate
            .state.filter { state -> state != CBManagerStatePoweredOn }
            .onEach(action)
            .launchIn(this)
    }

    private fun createPeripheralDelegate() = PeripheralDelegate(
        canSendWriteWithoutResponse,
        observers.characteristicChanges,
        logging,
        cbPeripheral.identifier.UUIDString,
    )

    override fun toString(): String = "Peripheral(cbPeripheral=$cbPeripheral)"
}

private val CBDescriptor.isUnsignedShortValue: Boolean
    get() = UUID.UUIDString.let {
        it == CBUUIDCharacteristicExtendedPropertiesString ||
            it == CBUUIDClientCharacteristicConfigurationString ||
            it == CBUUIDServerCharacteristicConfigurationString ||
            it == CBUUIDL2CAPPSMCharacteristicString
    }

private val Any?.type: String?
    get() = this?.let { it::class.simpleName }
