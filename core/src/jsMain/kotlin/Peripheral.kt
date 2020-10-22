@file:Suppress("RedundantUnitReturnType")

package com.juul.kable

import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import com.juul.kable.external.BluetoothDevice
import com.juul.kable.external.BluetoothRemoteGATTCharacteristic
import com.juul.kable.external.BluetoothRemoteGATTDescriptor
import com.juul.kable.external.BluetoothRemoteGATTServer
import com.juul.kable.external.string
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView
import org.khronos.webgl.Int8Array
import kotlin.coroutines.CoroutineContext
import org.w3c.dom.events.Event as JsEvent

private const val GATT_SERVER_DISCONNECTED = "gattserverdisconnected"

internal fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
) = Peripheral(coroutineContext, bluetoothDevice)

public actual class Peripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val bluetoothDevice: BluetoothDevice,
) {

    private val job = Job(parentCoroutineContext[Job]).apply {
        invokeOnCompletion {
            console.log("Shutting down job")
            console.dir(this@Peripheral)
            observers.clear()
            disconnectGatt()
            unregisterDisconnectedListener()
            console.log("Shutting down job complete")
        }
    }
    private val scope = CoroutineScope(parentCoroutineContext + job)

    private val _state = MutableStateFlow<State?>(null)
    public actual val state: Flow<State> = _state.filterNotNull()

    private val _events = MutableSharedFlow<Event>()
    public actual val events: Flow<Event> = _events.asSharedFlow()

    private var _services: List<PlatformService>? = null
    public actual val services: List<DiscoveredService>?
        get() = _services?.map { it.toDiscoveredService() }

    public actual suspend fun rssi(): Int {
        TODO("Not yet implemented")
    }

    private val gatt: BluetoothRemoteGATTServer
        get() = bluetoothDevice.gatt!! // fixme: !!

    public actual suspend fun connect() {
        _state.value = State.Connecting

        try {
            registerDisconnectedListener()
            gatt.connect().await() // todo: Catch appropriate exception to emit State.Rejected.
            discoverServices()
            _events.emit(Event.Connected(this))
            observers.rewire(_services!!)
            _state.value = State.Connected
        } catch (cancellation: CancellationException) {
            disconnectGatt()
            throw cancellation
        }
    }

    public actual suspend fun disconnect() {
        console.log("Initiating disconnect")
        scope.coroutineContext[Job]?.cancelAndJoinChildren()
        disconnectGatt()
        console.log("Disconnect complete")
    }

    private fun disconnectGatt() {
        _state.value = State.Disconnecting
        bluetoothDevice.gatt?.disconnect()
    }

    private suspend fun discoverServices() {
        console.log("Discovering services")
        _services = gatt.getPrimaryServices()
            .await()
            .map { service -> service.toPlatformService() }
        console.log("Service discovery complete")
    }

    public actual suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ) {
        bluetoothRemoteGATTCharacteristicFrom(characteristic).run {
            when (writeType) {
                WithResponse -> writeValueWithResponse(data)
                WithoutResponse -> writeValueWithResponse(data)
            }
        }.await()
    }

    public suspend fun readAsDataView(
        characteristic: Characteristic
    ): DataView = bluetoothRemoteGATTCharacteristicFrom(characteristic)
        .readValue()
        .await()

    public actual suspend fun read(
        characteristic: Characteristic
    ): ByteArray = readAsDataView(characteristic)
        .buffer
        .toByteArray()

    public actual suspend fun write(
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

    public actual suspend fun read(
        descriptor: Descriptor
    ): ByteArray = readAsDataView(descriptor)
        .buffer
        .toByteArray()

    private val observers = Observers(this)

    public fun observeDataView(
        characteristic: Characteristic
    ): Flow<DataView> = observers.acquire(characteristic)

    public actual fun observe(
        characteristic: Characteristic
    ): Flow<ByteArray> = observeDataView(characteristic)
        .map { it.buffer.toByteArray() }

    internal fun bluetoothRemoteGATTCharacteristicFrom(
        characteristic: Characteristic
    ): BluetoothRemoteGATTCharacteristic {
        val services = checkNotNull(_services) { "Services have not been discovered for $this" }
        val characteristics = services
            .first { it.serviceUuid == characteristic.serviceUuid }
            .characteristics
        return characteristics
            .first { it.characteristicUuid == characteristic.characteristicUuid }
            .bluetoothRemoteGATTCharacteristic
    }

    private fun bluetoothRemoteGATTDescriptorFrom(
        descriptor: Descriptor
    ): BluetoothRemoteGATTDescriptor {
        val services = checkNotNull(_services) { "Services have not been discovered for $this" }
        val characteristics = services
            .first { service -> service.serviceUuid == descriptor.serviceUuid }
            .characteristics
        val descriptors = characteristics
            .first { it.characteristicUuid == descriptor.characteristicUuid }
            .descriptors
        return descriptors
            .first { it.descriptorUuid == descriptor.descriptorUuid }
            .bluetoothRemoteGATTDescriptor
    }

    private var isDisconnectedListenerRegistered = false
    private val disconnectedListener: (JsEvent) -> Unit = {
        observers.invalidate()
        _state.value = State.Disconnected(cause = null) // `cause` is unavailable in Javascript.
        scope.launch {
            _events.emit(Event.Disconnected(wasConnected = true))
        }
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

    override fun toString(): String = "Peripheral(bluetoothDevice=${bluetoothDevice.string()})"
}

private fun ArrayBuffer.toByteArray(): ByteArray = Int8Array(this).unsafeCast<ByteArray>()
