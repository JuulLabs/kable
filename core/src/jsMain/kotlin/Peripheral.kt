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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView
import org.khronos.webgl.Int8Array
import kotlin.coroutines.CoroutineContext
import org.w3c.dom.events.Event as JsEvent

private const val GATT_SERVER_DISCONNECTED = "gattserverdisconnected"

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

    private val job = Job(parentCoroutineContext.job).apply {
        invokeOnCompletion { dispose() }
    }

    private val scope = CoroutineScope(parentCoroutineContext + job)

    private val _state = MutableStateFlow<State?>(null)
    public override val state: Flow<State> = _state.filterNotNull()

    private var platformServices: List<PlatformService>? = null
    public override val services: List<DiscoveredService>?
        get() = platformServices?.map { it.toDiscoveredService() }

    public override suspend fun rssi(): Int {
        TODO("Not yet implemented")
    }

    private val gatt: BluetoothRemoteGATTServer
        get() = bluetoothDevice.gatt!! // fixme: !!

    private var connectJob: Job? = null

    private fun createConnectJob() = scope.launch(start = CoroutineStart.LAZY) {
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
        val job = connectJob ?: createConnectJob().also { connectJob = it }
        job.join()
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
        platformServices = services
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

    override fun toString(): String = "Peripheral(bluetoothDevice=${bluetoothDevice.string()})"
}

private fun ArrayBuffer.toByteArray(): ByteArray = Int8Array(this).unsafeCast<ByteArray>()
