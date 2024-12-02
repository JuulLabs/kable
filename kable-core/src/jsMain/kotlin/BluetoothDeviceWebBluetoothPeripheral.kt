@file:Suppress("RedundantUnitReturnType")

package com.juul.kable

import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import com.juul.kable.bluetooth.isWatchingAdvertisementsSupported
import com.juul.kable.external.BluetoothAdvertisingEvent
import com.juul.kable.external.BluetoothDevice
import com.juul.kable.external.BluetoothRemoteGATTServer
import com.juul.kable.external.string
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.logs.Logging.DataProcessor.Operation
import com.juul.kable.logs.detail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.DataView
import org.w3c.dom.events.EventListener
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration

private const val ADVERTISEMENT_RECEIVED = "advertisementreceived"

internal class BluetoothDeviceWebBluetoothPeripheral(
    private val bluetoothDevice: BluetoothDevice,
    observationExceptionHandler: ObservationExceptionHandler,
    private val onServicesDiscovered: ServicesDiscoveredAction,
    private val disconnectTimeout: Duration,
    private val logging: Logging,
) : BasePeripheral(bluetoothDevice.id), WebBluetoothPeripheral {

    private val connectAction = sharedRepeatableAction(::establishConnection)

    private val logger = Logger(logging, identifier = bluetoothDevice.id)

    private val _state = MutableStateFlow<State>(State.Disconnected())
    override val state: StateFlow<State> = _state.asStateFlow()

    override val identifier: String = bluetoothDevice.id

    private val connection = MutableStateFlow<Connection?>(null)
    private fun connectionOrThrow() = connection.value
        ?: throw NotConnectedException("Connection not established, current state: ${state.value}")

    private val _services = MutableStateFlow<List<DiscoveredService>?>(null)
    override val services = _services.asStateFlow()
    private fun servicesOrThrow() = services.value ?: error("Services have not been discovered")

    @ExperimentalApi
    override val name: String? get() = bluetoothDevice.name

    private val observers = Observers<DataView>(this, logging, observationExceptionHandler)

    private suspend fun establishConnection(scope: CoroutineScope): CoroutineScope {
        logger.info { message = "Connecting" }
        _state.value = State.Connecting.Bluetooth

        try {
            connection.value = Connection(
                scope.coroutineContext,
                bluetoothDevice,
                _state,
                _services,
                observers.characteristicChanges,
                disconnectTimeout,
                logging,
            )
            connectionOrThrow().execute(BluetoothRemoteGATTServer::connect)
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

    private suspend fun discoverServices() {
        connectionOrThrow().discoverServices()
        unwrapCancellationExceptions {
            onServicesDiscovered(ServicesDiscoveredPeripheral(this))
        }
    }

    private suspend fun configureCharacteristicObservations() {
        _state.value = State.Connecting.Observes
        logger.verbose { message = "Configuring characteristic observations" }
        observers.onConnected()
    }

    override suspend fun connect(): CoroutineScope =
        connectAction.await()

    override suspend fun disconnect() {
        connectAction.cancelAndJoin(
            CancellationException(NotConnectedException("Disconnect requested")),
        )
    }

    /**
     * Per [Web Bluetooth / Scanning Sample][https://googlechrome.github.io/samples/web-bluetooth/scan.html]:
     *
     * > Scanning is still under development. You must be using Chrome 79+ with the
     * > `chrome://flags/#enable-experimental-web-platform-features` flag enabled.
     *
     * Note that even with the above flag enabled (as of Chrome 128)
     * [BluetoothDevice.unwatchAdvertisements] was not available.
     *
     * Overview of Chrome's Web Bluetooth implementation status can be found at:
     * https://github.com/WebBluetoothCG/web-bluetooth/blob/main/implementation-status.md#chrome
     *
     * @throws UnsupportedOperationException If feature is not enabled and/or supported.
     */
    @ExperimentalApi
    override suspend fun rssi(): Int {
        if (!isWatchingAdvertisementsSupported) {
            throw UnsupportedOperationException("RSSI not supported")
        }

        return coroutineScope {
            val rssi = async(start = UNDISPATCHED) { receiveRssiEvent() }
            var didWatch = false
            try {
                if (!bluetoothDevice.watchingAdvertisements) {
                    logger.verbose { message = "watchAdvertisements" }
                    bluetoothDevice.watchAdvertisements().await()
                    didWatch = true
                }
                rssi.await()
            } finally {
                if (didWatch) {
                    logger.verbose { message = "unwatchAdvertisements" }
                    bluetoothDevice.unwatchAdvertisements().await()
                }
            }
        }
    }

    private suspend fun receiveRssiEvent() = suspendCancellableCoroutine { continuation ->
        val listener = EventListener { event ->
            val rssi = event.unsafeCast<BluetoothAdvertisingEvent>().rssi
            if (rssi != null) {
                continuation.resume(rssi)
            } else {
                continuation.resumeWithException(
                    InternalError("BluetoothAdvertisingEvent.rssi was null"),
                )
            }
        }

        logger.verbose {
            message = "addEventListener"
            detail("event", ADVERTISEMENT_RECEIVED)
        }
        bluetoothDevice.addEventListener(ADVERTISEMENT_RECEIVED, listener)

        continuation.invokeOnCancellation {
            logger.verbose {
                message = "removeEventListener"
                detail("event", ADVERTISEMENT_RECEIVED)
            }
            bluetoothDevice.removeEventListener(ADVERTISEMENT_RECEIVED, listener)
        }
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
        connectionOrThrow().execute {
            when (writeType) {
                WithResponse -> platformCharacteristic.writeValueWithResponse(data)
                WithoutResponse -> platformCharacteristic.writeValueWithoutResponse(data)
            }
        }
    }

    override suspend fun readAsDataView(
        characteristic: Characteristic,
    ): DataView {
        val platformCharacteristic = servicesOrThrow().obtain(characteristic, Read)
        val value = connectionOrThrow().execute {
            platformCharacteristic.readValue()
        }
        logger.debug {
            message = "read"
            detail(characteristic)
            detail(value, Operation.Read)
        }
        return value
    }

    override suspend fun read(
        characteristic: Characteristic,
    ): ByteArray = readAsDataView(characteristic)
        .buffer
        .toByteArray()

    override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ) {
        logger.debug {
            message = "write"
            detail(descriptor)
            detail(data, Operation.Write)
        }

        val platformDescriptor = servicesOrThrow().obtain(descriptor)
        connectionOrThrow().execute {
            platformDescriptor.writeValue(data)
        }
    }

    override suspend fun readAsDataView(
        descriptor: Descriptor,
    ): DataView {
        val platformDescriptor = servicesOrThrow().obtain(descriptor)
        val value = connectionOrThrow().execute {
            platformDescriptor.readValue()
        }
        logger.debug {
            message = "read"
            detail(descriptor)
            detail(value, Operation.Read)
        }
        return value
    }

    override suspend fun read(
        descriptor: Descriptor,
    ): ByteArray = readAsDataView(descriptor)
        .buffer
        .toByteArray()

    override fun observeDataView(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<DataView> = observers.acquire(characteristic, onSubscription)

    override fun observe(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<ByteArray> = observeDataView(characteristic, onSubscription)
        .map { it.buffer.toByteArray() }

    internal suspend fun startObservation(characteristic: Characteristic) {
        connectionOrThrow().startObservation(characteristic)
    }

    internal suspend fun stopObservation(characteristic: Characteristic) {
        connectionOrThrow().stopObservation(characteristic)
    }

    override fun toString(): String = "Peripheral(bluetoothDevice=${bluetoothDevice.string()})"
}
