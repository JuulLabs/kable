@file:Suppress("RedundantUnitReturnType")

package com.juul.kable

import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import com.juul.kable.external.BluetoothAdvertisingEvent
import com.juul.kable.external.BluetoothDevice
import com.juul.kable.external.BluetoothRemoteGATTCharacteristic
import com.juul.kable.external.BluetoothRemoteGATTServer
import com.juul.kable.external.string
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.logs.detail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.khronos.webgl.DataView
import kotlin.coroutines.CoroutineContext
import org.w3c.dom.events.Event as JsEvent

private const val GATT_SERVER_DISCONNECTED = "gattserverdisconnected"
private const val ADVERTISEMENT_RECEIVED = "advertisementreceived"
private const val CHARACTERISTIC_VALUE_CHANGED = "characteristicvaluechanged"

private const val DISCONNECTION_DELAY = 1_500L

private typealias ObservationListener = (JsEvent) -> Unit

public actual fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral = peripheral(advertisement.bluetoothDevice, builderAction)

internal fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    builderAction: PeripheralBuilderAction = {},
): JsPeripheral {
    val builder = PeripheralBuilder()
    builder.builderAction()
    return JsPeripheral(
        coroutineContext,
        bluetoothDevice,
        builder.onServicesDiscovered,
        builder.logging,
    )
}

public class JsPeripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val bluetoothDevice: BluetoothDevice,
    private val onServicesDiscovered: ServicesDiscoveredAction,
    logging: Logging,
) : Peripheral {

    private val job = SupervisorJob(parentCoroutineContext.job).apply {
        invokeOnCompletion { finalCleanup() }
    }

    private val scope = CoroutineScope(parentCoroutineContext + job)

    private val logger = Logger(logging, identifier = bluetoothDevice.id)

    private val ioLock = Mutex()

    internal val platformIdentifier = bluetoothDevice.id

    private val _state = MutableStateFlow<State>(State.Disconnected())
    public override val state: StateFlow<State> = _state.asStateFlow()

    private var _platformServices: List<PlatformService>? = null
    private val platformServices: List<PlatformService>
        get() = checkNotNull(_platformServices) { "Services have not been discovered for $this" }

    public override val services: List<DiscoveredService>?
        get() = _platformServices?.map { it.toDiscoveredService() }

    private val observationListeners = mutableMapOf<Characteristic, ObservationListener>()

    private val supportsAdvertisements = js("BluetoothDevice.prototype.watchAdvertisements") != null

    public override suspend fun rssi(): Int = suspendCancellableCoroutine { continuation ->
        check(supportsAdvertisements) { "watchAdvertisements unavailable" }

        lateinit var listener: (JsEvent) -> Unit
        val cleanup = {
            bluetoothDevice.removeEventListener(ADVERTISEMENT_RECEIVED, listener)
            // At the time of writing `unwatchAdvertisements()` remains unimplemented
            if (bluetoothDevice.watchingAdvertisements && js("BluetoothDevice.prototype.unwatchAdvertisements") != null) {
                bluetoothDevice.unwatchAdvertisements()
            }
        }

        listener = {
            val event = it as BluetoothAdvertisingEvent
            cleanup()
            if (continuation.isActive && event.rssi != null) {
                continuation.resume(event.rssi, onCancellation = null)
            }
        }

        if (!bluetoothDevice.watchingAdvertisements) {
            bluetoothDevice.watchAdvertisements()
        }
        bluetoothDevice.addEventListener(ADVERTISEMENT_RECEIVED, listener)

        continuation.invokeOnCancellation {
            cleanup()
        }
    }

    private val gatt: BluetoothRemoteGATTServer
        get() = bluetoothDevice.gatt!! // fixme: !!

    private var connectJob: Deferred<Unit>? = null

    private fun connectAsync() = scope.async(start = LAZY) {
        try {
            openConnection()
        } catch (t: Throwable) {
            logger.error(t) { message = "Failed to connect" }
            scope.async {
                disconnectSync(cancelConnectJob = false)
            }
            throw t
        }
    }

    private var disconnectJob: Deferred<Unit>? = null

    private fun disconnectAsync(
        cancelConnectJob: Boolean,
    ) = scope.async {
        if (cancelConnectJob) {
            connectJob?.cancelAndJoin()
        } else {
            connectJob?.join()
        }
        closeConnection()
    }

    public override suspend fun connect() {
        connectSync()
    }

    public override suspend fun disconnect() {
        disconnectSync(cancelConnectJob = true)
    }

    private suspend fun connectSync() {
        val job = connectJob ?: connectAsync().apply {
            connectJob = this
            invokeOnCompletion { connectJob = null }
        }
        job.await()
    }

    private suspend fun disconnectSync(
        cancelConnectJob: Boolean
    ) {
        val job = disconnectJob ?: disconnectAsync(cancelConnectJob).apply {
            disconnectJob = this
            invokeOnCompletion { disconnectJob = null }
        }
        job.await()
    }

    private suspend fun openConnection() {
        // Guard if already connected do nothing
        if (_state.value is State.Connected) return

        logger.info { message = "Connecting" }
        // Wait until fully disconnected before proceeding
        _state.first { it is State.Disconnected }

        _state.value = State.Connecting.Bluetooth
        registerDisconnectedListener()
        gatt.connect().await() // todo: Catch appropriate exception to emit State.Rejected.

        _state.value = State.Connecting.Services
        discoverServices()
        onServicesDiscovered(ServicesDiscoveredPeripheral(this@JsPeripheral))

        _state.value = State.Connecting.Observes
        logger.verbose { message = "Configuring characteristic observations" }
        observers.onConnected()

        logger.info { message = "Connected" }
        _state.value = State.Connected
    }

    private suspend fun closeConnection() {
        // Guard if already disconnecting/disconnected do nothing
        if (_state.value is State.Disconnecting || _state.value is State.Disconnected) return

        try {
            _state.value = State.Disconnecting
            unregisterDisconnectedListener()
            stopAllObservations()
            bluetoothDevice.gatt?.disconnect()
            // FIXME: https://github.com/JuulLabs/kable/issues/251
            delay(DISCONNECTION_DELAY)
        } finally {
            _state.value = State.Disconnected()
        }
    }

    private fun finalCleanup() {
        try {
            unregisterDisconnectedListener()
            clearObservationsWithoutStopping()
            bluetoothDevice.gatt?.disconnect()
        } finally {
            // Avoid trampling existing `Disconnected` state (and its properties) by only updating if not already `Disconnected`.
            _state.update { previous -> previous as? State.Disconnected ?: State.Disconnected() }
        }
    }

    private suspend fun discoverServices() {
        logger.verbose { message = "discover services" }
        val services = ioLock.withLock {
            gatt.getPrimaryServices().await()
                .map { it.toPlatformService(logger) }
        }
        _platformServices = services
    }

    public override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ) {
        logger.debug {
            message = "write"
            detail(characteristic)
            detail(data)
        }

        val jsCharacteristic = bluetoothRemoteGATTCharacteristicFrom(characteristic)
        ioLock.withLock {
            when (writeType) {
                WithResponse -> jsCharacteristic.writeValueWithResponse(data)
                WithoutResponse -> jsCharacteristic.writeValueWithoutResponse(data)
            }.await()
        }
    }

    public suspend fun readAsDataView(
        characteristic: Characteristic
    ): DataView {
        val jsCharacteristic = bluetoothRemoteGATTCharacteristicFrom(characteristic)
        val value = ioLock.withLock {
            jsCharacteristic.readValue().await()
        }
        logger.debug {
            message = "read"
            detail(characteristic)
            detail(value)
        }
        return value
    }

    public override suspend fun read(
        characteristic: Characteristic
    ): ByteArray = readAsDataView(characteristic)
        .buffer
        .toByteArray()

    public override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray
    ) {
        logger.debug {
            message = "write"
            detail(descriptor)
            detail(data)
        }

        val jsDescriptor = bluetoothRemoteGATTDescriptorFrom(descriptor)
        ioLock.withLock {
            jsDescriptor.writeValue(data).await()
        }
    }

    public suspend fun readAsDataView(
        descriptor: Descriptor
    ): DataView {
        val jsDescriptor = bluetoothRemoteGATTDescriptorFrom(descriptor)
        val value = ioLock.withLock {
            jsDescriptor.readValue().await()
        }
        logger.debug {
            message = "read"
            detail(descriptor)
            detail(value)
        }
        return value
    }

    public override suspend fun read(
        descriptor: Descriptor
    ): ByteArray = readAsDataView(descriptor)
        .buffer
        .toByteArray()

    private val observers = Observers<DataView>(this, logging, extraBufferCapacity = 64)

    public fun observeDataView(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction = {},
    ): Flow<DataView> = observers.acquire(characteristic, onSubscription)

    public override fun observe(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<ByteArray> = observeDataView(characteristic, onSubscription)
        .map { it.buffer.toByteArray() }

    private var isDisconnectedListenerRegistered = false
    private val disconnectedListener: (JsEvent) -> Unit = {
        logger.debug { message = GATT_SERVER_DISCONNECTED }
        scope.async {
            disconnectSync(cancelConnectJob = true)
        }
    }

    internal suspend fun startObservation(characteristic: Characteristic) {
        if (characteristic in observationListeners) return
        logger.debug {
            message = "observe start"
            detail(characteristic)
        }

        val listener = characteristic.createListener()
        observationListeners[characteristic] = listener

        bluetoothRemoteGATTCharacteristicFrom(characteristic).apply {
            addEventListener(CHARACTERISTIC_VALUE_CHANGED, listener)
            ioLock.withLock {
                withContext(NonCancellable) {
                    startNotifications().await()
                }
            }
        }
    }

    internal suspend fun stopObservation(characteristic: Characteristic) {
        val listener = observationListeners.remove(characteristic) ?: return
        logger.verbose {
            message = "observe stop"
            detail(characteristic)
        }

        bluetoothRemoteGATTCharacteristicFrom(characteristic).apply {
            /* Throws `DOMException` if connection is closed:
             *
             * DOMException: Failed to execute 'stopNotifications' on 'BluetoothRemoteGATTCharacteristic':
             * Characteristic with UUID [...] is no longer valid. Remember to retrieve the characteristic
             * again after reconnecting.
             *
             * Wrapped in `runCatching` to silently ignore failure, as notification will already be
             * invalidated due to the connection being closed.
             */
            runCatching {
                ioLock.withLock {
                    withContext(NonCancellable) {
                        stopNotifications().await()
                    }
                }
            }.onFailure {
                logger.warn {
                    message = "Stop notification failure ignored."
                    detail(characteristic)
                }
            }

            removeEventListener(CHARACTERISTIC_VALUE_CHANGED, listener)
        }
    }

    private suspend fun stopAllObservations() {
        observationListeners.keys.forEach { characteristic ->
            stopObservation(characteristic)
        }
    }

    private fun clearObservationsWithoutStopping() {
        observationListeners.forEach { (characteristic, listener) ->
            bluetoothRemoteGATTCharacteristicFrom(characteristic)
                .removeEventListener(CHARACTERISTIC_VALUE_CHANGED, listener)
        }
        observationListeners.clear()
    }

    private fun Characteristic.createListener(): ObservationListener = { event ->
        val target = event.target as BluetoothRemoteGATTCharacteristic
        val data = target.value!!
        logger.debug {
            message = CHARACTERISTIC_VALUE_CHANGED
            detail(this@createListener)
            detail(data)
        }
        val characteristicChange = ObservationEvent.CharacteristicChange(this, data)

        if (!observers.characteristicChanges.tryEmit(characteristicChange))
            console.error("Failed to emit $characteristicChange")
    }

    private fun registerDisconnectedListener() {
        if (isDisconnectedListenerRegistered) return
        isDisconnectedListenerRegistered = true
        bluetoothDevice.addEventListener(GATT_SERVER_DISCONNECTED, disconnectedListener)
    }

    private fun unregisterDisconnectedListener() {
        isDisconnectedListenerRegistered = false
        bluetoothDevice.removeEventListener(GATT_SERVER_DISCONNECTED, disconnectedListener)
    }

    private fun bluetoothRemoteGATTCharacteristicFrom(
        characteristic: Characteristic
    ) = platformServices.findCharacteristic(characteristic).bluetoothRemoteGATTCharacteristic

    private fun bluetoothRemoteGATTDescriptorFrom(
        descriptor: Descriptor
    ) = platformServices.findDescriptor(descriptor).bluetoothRemoteGATTDescriptor

    override fun toString(): String = "Peripheral(bluetoothDevice=${bluetoothDevice.string()})"
}

internal actual val Peripheral.identifier: String
    get() = (this as JsPeripheral).platformIdentifier
