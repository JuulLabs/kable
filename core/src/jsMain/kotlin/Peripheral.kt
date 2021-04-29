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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import org.khronos.webgl.DataView
import kotlin.coroutines.CoroutineContext
import org.w3c.dom.events.Event as JsEvent

private const val GATT_SERVER_DISCONNECTED = "gattserverdisconnected"
private const val ADVERTISEMENT_RECEIVED = "advertisementreceived"

public actual fun CoroutineScope.peripheral(
    advertisement: Advertisement,
): Peripheral = peripheral(advertisement.bluetoothDevice)

internal fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
) = JsPeripheral(coroutineContext, bluetoothDevice)

public class JsPeripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val bluetoothDevice: BluetoothDevice,
) : Peripheral {

    private val job = SupervisorJob(parentCoroutineContext.job).apply {
        invokeOnCompletion { dispose() }
    }

    private val scope = CoroutineScope(parentCoroutineContext + job)

    private val _state = MutableStateFlow<State?>(null)
    public override val state: Flow<State> = _state.filterNotNull()

    private var _platformServices: List<PlatformService>? = null
    private val platformServices: List<PlatformService>
        get() = checkNotNull(_platformServices) { "Services have not been discovered for $this" }

    public override val services: List<DiscoveredService>?
        get() = _platformServices?.map { it.toDiscoveredService() }

    private val supportsAdvertisements = js("BluetoothDevice.prototype.watchAdvertisements") != null

    public val advertisements: Flow<Advertisement> = callbackFlow {
        check(supportsAdvertisements) { "watchAdvertisements unavailable" }

        val listener: (JsEvent) -> Unit = {
            runCatching {
                offer(Advertisement(it as BluetoothAdvertisingEvent))
            }.onFailure {
                console.warn("Unable to deliver advertisement event due to failure in flow or premature closing.")
            }
        }

        if (!bluetoothDevice.watchingAdvertisements) {
            bluetoothDevice.watchAdvertisements()
        }
        bluetoothDevice.addEventListener(ADVERTISEMENT_RECEIVED, listener)

        awaitClose {
            bluetoothDevice.removeEventListener(ADVERTISEMENT_RECEIVED, listener)
            // At the time of writing `unwatchAdvertisements()` remains unimplemented
            if (bluetoothDevice.watchingAdvertisements && js("BluetoothDevice.prototype.unwatchAdvertisements") != null) {
                bluetoothDevice.unwatchAdvertisements()
            }
        }
    }

    public override suspend fun rssi(): Int =
        advertisements.first().rssi

    private val gatt: BluetoothRemoteGATTServer
        get() = bluetoothDevice.gatt!! // fixme: !!

    private var connectJob: Deferred<Unit>? = null

    private fun connectAsync() = scope.async(start = LAZY) {
        _state.value = State.Connecting

        try {
            registerDisconnectedListener()

            gatt.connect().await() // todo: Catch appropriate exception to emit State.Rejected.
            _state.value = State.Connected

            val services = discoverServices()
            observers.rewire(services)
        } catch (cancellation: CancellationException) {
            disconnectGatt()
            throw cancellation
        }
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
        val services = gatt.getPrimaryServices()
            .await()
            .map { it.toPlatformService() }
        _platformServices = services
        return services
    }

    public override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ) {
        bluetoothRemoteGATTCharacteristicFrom(characteristic).run {
            when (writeType) {
                WithResponse -> writeValueWithResponse(data)
                WithoutResponse -> writeValueWithoutResponse(data)
            }
        }.await()
    }

    public suspend fun readAsDataView(
        characteristic: Characteristic
    ): DataView = bluetoothRemoteGATTCharacteristicFrom(characteristic)
        .readValue()
        .await()

    public override suspend fun read(
        characteristic: Characteristic
    ): ByteArray = readAsDataView(characteristic)
        .buffer
        .toByteArray()

    public override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray
    ) {
        bluetoothRemoteGATTDescriptorFrom(descriptor)
            .writeValue(data)
            .await()
    }

    public suspend fun readAsDataView(
        descriptor: Descriptor
    ): DataView = bluetoothRemoteGATTDescriptorFrom(descriptor)
        .readValue()
        .await()

    public override suspend fun read(
        descriptor: Descriptor
    ): ByteArray = readAsDataView(descriptor)
        .buffer
        .toByteArray()

    private val observers = Observers(this)

    public fun observeDataView(
        characteristic: Characteristic
    ): Flow<DataView> = observers.acquire(characteristic)

    public override fun observe(
        characteristic: Characteristic
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
