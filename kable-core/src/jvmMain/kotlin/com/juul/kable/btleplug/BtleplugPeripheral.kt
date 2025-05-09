package com.juul.kable.btleplug

import com.juul.kable.BasePeripheral
import com.juul.kable.Characteristic
import com.juul.kable.Descriptor
import com.juul.kable.DiscoveredService
import com.juul.kable.ExperimentalApi
import com.juul.kable.Identifier
import com.juul.kable.NotConnectedException
import com.juul.kable.ObservationEvent.Disconnected.characteristic
import com.juul.kable.OnSubscriptionAction
import com.juul.kable.State
import com.juul.kable.State.Disconnected
import com.juul.kable.WriteType
import com.juul.kable.awaitConnect
import com.juul.kable.btleplug.ffi.PeripheralCallbacks
import com.juul.kable.btleplug.ffi.getPeripheral
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.sharedRepeatableAction
import com.juul.kable.suspendUntil
import jdk.internal.org.objectweb.asm.Type.getDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import com.juul.kable.btleplug.ffi.Uuid as FfiUuid

@OptIn(ExperimentalApi::class)
internal class BtleplugPeripheral(
    override val identifier: Identifier,
    logging: Logging,
) : BasePeripheral(identifier) {

    private val logger = Logger(logging, "Kable/Peripheral", identifier.toString())
    private val connectAction = scope.sharedRepeatableAction(::establishConnection)

    private val _state = MutableStateFlow<State>(Disconnected())
    override val state = _state.asStateFlow()

    private val _services = MutableStateFlow<List<DiscoveredService>?>(null)
    override val services = _services.asStateFlow()

    private val callbacks = object : PeripheralCallbacks {
        override fun connected() {
            _state.value = State.Connecting.Services
        }

        override fun disconnected() {
            scope.launch { disconnect() }
        }

        override fun notification(uuid: FfiUuid, data: ByteArray) {
            // TODO: Not yet implemented
        }
    }

    private val peripheral = scope.async { getPeripheral(identifier, callbacks) }

    override val name: String?
        get() = runBlocking { peripheral.await().properties().localName }

    private suspend fun getCharacteristic(service: FfiUuid, characteristic: FfiUuid) =
        peripheral.await().services()
            .single { it.uuid == service }
            .characteristics
            .single { it.uuid == characteristic }

    private suspend fun getDescriptor(service: FfiUuid, characteristic: FfiUuid, descriptor: FfiUuid) =
        getCharacteristic(service, characteristic)
            .descriptors
            .single { it.uuid == descriptor }

    private suspend fun establishConnection(scope: CoroutineScope): CoroutineScope {
        // TODO: Check Bluetooth is on/supported

        logger.info { message = "Connecting" }
        _state.value = State.Connecting.Bluetooth
        if (!peripheral.await().connect()) {
            throw IOException("Failed to connect.")
        }
        suspendUntil<State.Connecting.Services>()

        logger.info { message = "Discovering services" }
        if (!peripheral.await().discoverServices()) {
            throw IOException("Failed to discover services")
        }
        _services.value = peripheral.await().services().map(::BtleplugService)

        logger.info { message = "Configuring characteristic observations" }
        _state.value = State.Connecting.Observes
        // TODO: configureCharacteristicObservations()
        delay(1.seconds)
        _state.value = State.Connected(scope)

        return scope
    }

    override suspend fun connect(): CoroutineScope =
        connectAction.awaitConnect()

    override suspend fun disconnect() {
        _state.value = State.Disconnecting
        connectAction.cancelAndJoin(CancellationException(NotConnectedException("Disconnect requested")))
        _state.value = Disconnected()
    }

    // STOPSHIP: Double check this, but seems to be the default.
    override suspend fun maximumWriteValueLengthForType(writeType: WriteType): Int = 20

    @ExperimentalApi
    override suspend fun rssi(): Int =
        peripheral.await().properties().rssi?.toInt() ?: Int.MIN_VALUE

    override suspend fun read(characteristic: Characteristic): ByteArray {
        val ffi = getCharacteristic(characteristic.serviceUuid.toString(), characteristic.characteristicUuid.toString())
        return peripheral.await().read(ffi)
    }

    override suspend fun read(descriptor: Descriptor): ByteArray {
        val ffi = getDescriptor(
            descriptor.serviceUuid.toString(),
            descriptor.characteristicUuid.toString(),
            descriptor.descriptorUuid.toString(),
        )
        return peripheral.await().readDescriptor(ffi)
    }

    override suspend fun write(characteristic: Characteristic, data: ByteArray, writeType: WriteType) {
        val ffi = getCharacteristic(characteristic.serviceUuid.toString(), characteristic.characteristicUuid.toString())
        return peripheral.await().write(ffi, data, writeType.ffi())
    }

    override suspend fun write(descriptor: Descriptor, data: ByteArray) {
        val ffi = getDescriptor(
            descriptor.serviceUuid.toString(),
            descriptor.characteristicUuid.toString(),
            descriptor.descriptorUuid.toString(),
        )
        return peripheral.await().writeDescriptor(ffi, data)
    }

    override fun observe(characteristic: Characteristic, onSubscription: OnSubscriptionAction): Flow<ByteArray> {
        TODO("Not yet implemented")
    }

    override fun close() {
        scope.cancel("$this closed")
    }

    override fun toString(): String = "Peripheral(identifier=$identifier)"
}

private fun WriteType.ffi() = when (this) {
    WriteType.WithResponse -> com.juul.kable.btleplug.ffi.WriteType.WITH_RESPONSE
    WriteType.WithoutResponse -> com.juul.kable.btleplug.ffi.WriteType.WITHOUT_RESPONSE
}
