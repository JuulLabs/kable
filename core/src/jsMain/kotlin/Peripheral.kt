@file:Suppress("RedundantUnitReturnType")

package com.juul.kable

import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import com.juul.kable.external.BluetoothAdvertisingEvent
import com.juul.kable.external.BluetoothDevice
import com.juul.kable.external.BluetoothRemoteGATTServer
import com.juul.kable.external.string
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.khronos.webgl.DataView
import kotlin.coroutines.CoroutineContext
import org.w3c.dom.events.Event as JsEvent

private const val GATT_SERVER_DISCONNECTED = "gattserverdisconnected"
private const val ADVERTISEMENT_RECEIVED = "advertisementreceived"

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
    return JsPeripheral(coroutineContext, bluetoothDevice, builder.onServicesDiscovered)
}

public class JsPeripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val bluetoothDevice: BluetoothDevice,
    private val onServicesDiscovered: ServicesDiscoveredAction,
) : Peripheral {

    private val job = SupervisorJob(parentCoroutineContext.job).apply {
        invokeOnCompletion { dispose() }
    }

    private val scope = CoroutineScope(parentCoroutineContext + job)

    internal val ioLock = Mutex()

    private val _state = MutableStateFlow<State?>(null)
    public override val state: Flow<State> = _state.filterNotNull()

    private var _platformServices: List<PlatformService>? = null
    private val platformServices: List<PlatformService>
        get() = checkNotNull(_platformServices) { "Services have not been discovered for $this" }

    public override val services: List<DiscoveredService>?
        get() = _platformServices?.map { it.toDiscoveredService() }

    private val supportsAdvertisements = js("BluetoothDevice.prototype.watchAdvertisements") != null

    private val ready = MutableStateFlow(false)
    internal suspend fun suspendUntilReady() {
        // fast path
        if (ready.value && _state.value == State.Connected) return

        // slow path
        combine(ready, state) { ready, state -> ready && state == State.Connected }.first { it }
    }

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
        ready.value = false
        _state.value = State.Connecting

        try {
            registerDisconnectedListener()

            gatt.connect().await() // todo: Catch appropriate exception to emit State.Rejected.
            _state.value = State.Connected

            val services = discoverServices()
            onServicesDiscovered(ServicesDiscoveredPeripheral(this@JsPeripheral))
            observers.rewire(services)
        } catch (cancellation: CancellationException) {
            disconnectGatt()
            throw cancellation
        }

        ready.value = true
    }

    private fun dispose() {
        observers.clear()
        disconnectGatt()
        unregisterDisconnectedListener()
        _state.value = State.Disconnected()
    }

    public override suspend fun connect() {
        check(job.isNotCancelled) { "Cannot connect, scope is cancelled for $this" }
        val job = connectJob ?: connectAsync().also { connectJob = it }
        job.await()
    }

    public override suspend fun disconnect() {
        job.cancelAndJoinChildren()
        connectJob = null
        disconnectGatt()
    }

    private fun disconnectGatt() {
        _state.value = State.Disconnecting
        bluetoothDevice.gatt?.disconnect()
    }

    private suspend fun discoverServices(): List<PlatformService> {
        val services = ioLock.withLock {
            gatt.getPrimaryServices().await()
        }.map { it.toPlatformService() }
        _platformServices = services
        return services
    }

    public override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ) {
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
        return ioLock.withLock {
            jsCharacteristic.readValue().await()
        }
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
        val jsDescriptor = bluetoothRemoteGATTDescriptorFrom(descriptor)
        ioLock.withLock {
            jsDescriptor.writeValue(data).await()
        }
    }

    public suspend fun readAsDataView(
        descriptor: Descriptor
    ): DataView {
        val jsDescriptor = bluetoothRemoteGATTDescriptorFrom(descriptor)
        return ioLock.withLock {
            jsDescriptor.readValue().await()
        }
    }

    public override suspend fun read(
        descriptor: Descriptor
    ): ByteArray = readAsDataView(descriptor)
        .buffer
        .toByteArray()

    private val observers = Observers(this)

    public fun observeDataView(
        characteristic: Characteristic,
        onObservationStarted: ObservationStartedAction = {},
    ): Flow<DataView> = observers.acquire(characteristic, onObservationStarted)

    public override fun observe(
        characteristic: Characteristic,
        onObservationStarted: ObservationStartedAction,
    ): Flow<ByteArray> = observeDataView(characteristic)
        .map { it.buffer.toByteArray() }

    private var isDisconnectedListenerRegistered = false
    private val disconnectedListener: (JsEvent) -> Unit = { event ->
        console.dir(event)
        observers.invalidate()
        _state.value = State.Disconnected()
        unregisterDisconnectedListener()
        connectJob = null
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

    internal fun bluetoothRemoteGATTCharacteristicFrom(
        characteristic: Characteristic
    ) = platformServices.findCharacteristic(characteristic).bluetoothRemoteGATTCharacteristic

    private fun bluetoothRemoteGATTDescriptorFrom(
        descriptor: Descriptor
    ) = platformServices.findDescriptor(descriptor).bluetoothRemoteGATTDescriptor

    override fun toString(): String = "Peripheral(bluetoothDevice=${bluetoothDevice.string()})"
}
