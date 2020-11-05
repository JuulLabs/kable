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
) = JsPeripheral(coroutineContext, bluetoothDevice)

public class JsPeripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val bluetoothDevice: BluetoothDevice,
) : Peripheral {

    private val job = Job(parentCoroutineContext[Job]).apply {
        invokeOnCompletion {
            console.log("Shutting down job")
            console.dir(this@JsPeripheral)
            observers.clear()
            disconnectGatt()
            unregisterDisconnectedListener()
            console.log("Shutting down job complete")
        }
    }
    private val scope = CoroutineScope(parentCoroutineContext + job)

    private val _state = MutableStateFlow<State?>(null)
    public override val state: Flow<State> = _state.filterNotNull()

    private val _events = MutableSharedFlow<Event>()
    public override val events: Flow<Event> = _events.asSharedFlow()

    private var platformServices: List<PlatformService>? = null
    public override val services: List<DiscoveredService>?
        get() = platformServices?.map { it.toDiscoveredService() }

    public override suspend fun rssi(): Int {
        TODO("Not yet implemented")
    }

    private val gatt: BluetoothRemoteGATTServer
        get() = bluetoothDevice.gatt!! // fixme: !!

    public override suspend fun connect() {
        // todo: Prevent multiple simultaneous connection attempts.
        _state.value = State.Connecting

        try {
            registerDisconnectedListener() // todo: Unregister on connection drop?
            gatt.connect().await() // todo: Catch appropriate exception to emit State.Rejected.
            _state.value = State.Connected
            val services = discoverServices()
            observers.rewire(services)
            _events.emit(Event.Ready)
        } catch (cancellation: CancellationException) {
            disconnectGatt()
            throw cancellation
        }
    }

    public override suspend fun disconnect() {
        console.log("Initiating disconnect")
        scope.coroutineContext[Job]?.cancelAndJoinChildren()
        disconnectGatt()
        console.log("Disconnect complete")
    }

    private fun disconnectGatt() {
        _state.value = State.Disconnecting
        bluetoothDevice.gatt?.disconnect()
    }

    private suspend fun discoverServices(): List<PlatformService> {
        console.log("Discovering services")
        val services = gatt.getPrimaryServices()
            .await()
            .map { it.toPlatformService() }
        platformServices = services
        console.log("Service discovery complete")
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
                WithoutResponse -> writeValueWithResponse(data)
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

    internal fun bluetoothRemoteGATTCharacteristicFrom(
        characteristic: Characteristic
    ): BluetoothRemoteGATTCharacteristic {
        val services = checkNotNull(platformServices) { "Services have not been discovered for $this" }
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
        val services = checkNotNull(platformServices) { "Services have not been discovered for $this" }
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
    private val disconnectedListener: (JsEvent) -> Unit = { event ->
        console.dir(event)
        observers.invalidate()
        _state.value = State.Disconnected()
        scope.launch {
            _events.emit(Event.Disconnected)
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
