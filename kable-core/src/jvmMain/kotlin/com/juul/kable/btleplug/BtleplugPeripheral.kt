package com.juul.kable.btleplug

import com.juul.kable.BasePeripheral
import com.juul.kable.Characteristic
import com.juul.kable.Descriptor
import com.juul.kable.DiscoveredService
import com.juul.kable.ExperimentalApi
import com.juul.kable.Identifier
import com.juul.kable.NotConnectedException
import com.juul.kable.ObservationEvent.CharacteristicChange
import com.juul.kable.Observers
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
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import com.juul.kable.btleplug.ffi.Uuid as FfiUuid

@OptIn(ExperimentalApi::class)
internal class BtleplugPeripheral(
    override val identifier: Identifier,
    logging: Logging,
) : BasePeripheral(identifier) {

    internal val logger = Logger(logging, "Kable/Peripheral", identifier.toString())
    private val connectAction = scope.sharedRepeatableAction(::establishConnection)

    private val _state = MutableStateFlow<State>(Disconnected())
    override val state = _state.asStateFlow()

    private val _services = MutableStateFlow<List<DiscoveredService>?>(null)
    override val services = _services.asStateFlow()

    private val callbacks: PeripheralCallbacks = object : PeripheralCallbacks {
        override fun connected() {
            _state.value = State.Connecting.Services
        }

        override fun disconnected() {
            scope.launch { disconnect() }
        }

        override suspend fun notification(uuid: FfiUuid, data: ByteArray) {
            val characteristic = peripheral.await()
                .services()
                .asSequence()
                .flatMap { it.characteristics }
                .single { it.uuid == uuid }
            observers.characteristicChanges.emit(
                CharacteristicChange(BtleplugCharacteristic(characteristic), data),
            )
        }
    }

    internal val peripheral = scope.async { getPeripheral(identifier.ffi, callbacks) }

    private val observers = Observers<ByteArray>(this, logging) { cause ->
        logger.error(cause) { "Exception in observers" }
    }

    override val name: String?
        get() = when (peripheral.isCompleted) {
            true -> runBlocking { peripheral.getCompleted().properties() }.localName
            false -> null
        }

    internal suspend fun getCharacteristic(characteristic: Characteristic) =
        getCharacteristic(characteristic.serviceUuid.toString(), characteristic.characteristicUuid.toString())

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

        logger.verbose { message = "Configuring characteristic observations" }
        _state.value = State.Connecting.Observes
        observers.onConnected()
        _state.value = State.Connected(scope)

        return scope
    }

    override suspend fun connect(): CoroutineScope =
        connectAction.awaitConnect()

    override suspend fun disconnect() {
        logger.verbose { "Disconnect request" }
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
        logger.verbose { "Reading from $characteristic" }
        val ffi = peripheral.await()
        return withContext(Dispatchers.IO) {
            ffi.read(getCharacteristic(characteristic))
        }
    }

    override suspend fun read(descriptor: Descriptor): ByteArray {
        logger.verbose { "Reading from $descriptor" }
        val ffi = peripheral.await()
        return withContext(Dispatchers.IO) {
            ffi.readDescriptor(
                getDescriptor(
                    descriptor.serviceUuid.toString(),
                    descriptor.characteristicUuid.toString(),
                    descriptor.descriptorUuid.toString(),
                ),
            )
        }
    }

    override suspend fun write(characteristic: Characteristic, data: ByteArray, writeType: WriteType) {
        logger.verbose { "Writing to $characteristic, type=$writeType data=${data.size} bytes" }
        val ffi = peripheral.await()
        return withContext(Dispatchers.IO) {
            ffi.write(getCharacteristic(characteristic), data, writeType.ffi())
        }
    }

    override suspend fun write(descriptor: Descriptor, data: ByteArray) {
        logger.verbose { "Writing to $descriptor, data=${data.size} bytes" }
        val ffi = peripheral.await()
        return withContext(Dispatchers.IO) {
            ffi.writeDescriptor(
                getDescriptor(
                    descriptor.serviceUuid.toString(),
                    descriptor.characteristicUuid.toString(),
                    descriptor.descriptorUuid.toString(),
                ),
                data,
            )
        }
    }

    override fun observe(characteristic: Characteristic, onSubscription: OnSubscriptionAction): Flow<ByteArray> =
        observers.acquire(characteristic, onSubscription)

    override fun close() {
        scope.cancel("$this closed")
    }

    override fun toString(): String = "Peripheral(identifier=$identifier)"
}

private fun WriteType.ffi() = when (this) {
    WriteType.WithResponse -> com.juul.kable.btleplug.ffi.WriteType.WITH_RESPONSE
    WriteType.WithoutResponse -> com.juul.kable.btleplug.ffi.WriteType.WITHOUT_RESPONSE
}
