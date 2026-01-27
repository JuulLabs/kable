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
import com.juul.kable.ServicesDiscoveredAction
import com.juul.kable.ServicesDiscoveredPeripheral
import com.juul.kable.State
import com.juul.kable.State.Connecting
import com.juul.kable.State.Disconnected
import com.juul.kable.State.Disconnecting
import com.juul.kable.WriteType
import com.juul.kable.awaitConnect
import com.juul.kable.btleplug.ffi.CancellationHandle
import com.juul.kable.btleplug.ffi.PeripheralCallbacks
import com.juul.kable.btleplug.ffi.isAdapterOn
import com.juul.kable.coroutines.childSupervisor
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.properties
import com.juul.kable.sharedRepeatableAction
import com.juul.kable.suspendUntil
import com.juul.kable.unwrapCancellationException
import jdk.internal.joptsimple.internal.Messages.message
import jdk.internal.org.objectweb.asm.Type.getDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import com.juul.kable.btleplug.ffi.Uuid as FfiUuid

private const val DEFAULT_ATT_MTU = 23
private const val ATT_MTU_HEADER_SIZE = 3

@OptIn(ExperimentalApi::class)
internal class BtleplugPeripheral(
    override val identifier: Identifier,
    private val onServicesDiscovered: ServicesDiscoveredAction,
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
            _state.value = Connecting.Services
        }

        override fun disconnected() {
            logger.verbose { message = "Received disconnect" }
            runBlocking {
                connectAction.cancelAndJoin(CancellationException(NotConnectedException("Disconnected")))
            }
        }

        override suspend fun notification(uuid: FfiUuid, data: ByteArray) {
            val characteristic = ffi
                .services()
                .asSequence()
                .flatMap { it.characteristics }
                .single { it.uuid == uuid }
            observers.characteristicChanges.emit(
                CharacteristicChange(BtleplugCharacteristic(characteristic), data),
            )
        }
    }

    internal val ffi = com.juul.kable.btleplug.ffi.Peripheral(identifier.ffi, callbacks)

    private val observers = Observers<ByteArray>(this, logging, false) { cause ->
        logger.error(cause) { message = "Exception in observers" }
    }

    private var _name: String? = null
    override val name: String?
        get() {
            try {
                _name = runBlocking { ffi.properties() }.localName
            } catch (_: Exception) {
                null
            }
            return _name
        }

    internal suspend fun getCharacteristic(characteristic: Characteristic) =
        getCharacteristic(
            characteristic.serviceUuid.toString(),
            characteristic.characteristicUuid.toString(),
        )

    private suspend fun getCharacteristic(service: FfiUuid, characteristic: FfiUuid) =
        withContext(Dispatchers.IO) {
            ffi.services()
                .single { it.uuid == service }
                .characteristics
                .single { it.uuid == characteristic }
        }

    private suspend fun getDescriptor(service: FfiUuid, characteristic: FfiUuid, descriptor: FfiUuid) =
        withContext(Dispatchers.IO) {
            getCharacteristic(service, characteristic)
                .descriptors
                .single { it.uuid == descriptor }
        }

    private suspend fun establishConnection(scope: CoroutineScope): CoroutineScope {
        val taskScope = scope.childSupervisor("$name/Tasks")
        val cancellationHandle = CancellationHandle()
        scope.launch(start = CoroutineStart.ATOMIC) {
            try {
                awaitCancellation()
            } finally {
                withContext(NonCancellable) {
                    _state.value = Disconnecting
                    taskScope.coroutineContext.job.cancelAndJoin()
                    ffi.disconnect()
                    _state.value = Disconnected()
                }
            }
        }

        if (!isAdapterOn()) {
            logger.error { message = "Bluetooth adapter is off" }
            throw IOException("Bluetooth adapter is off")
        }

        logger.info { message = "Connecting" }
        _state.value = Connecting.Bluetooth
        try {
            if (!ffi.connect(cancellationHandle)) {
                throw IOException("Failed to connect.")
            }
            suspendUntil<Connecting.Services>()

            logger.info { message = "Discovering services" }
            ffi.discoverServices()
            _services.value = ffi.services().map(::BtleplugService)
            ServicesDiscoveredPeripheral(this).run { onServicesDiscovered() }

            logger.verbose { message = "Configuring characteristic observations" }
            _state.value = Connecting.Observes
            observers.onConnected()
        } catch (e: Exception) {
            val failure = e.unwrapCancellationException()
            logger.error(failure) { message = "Failed to establish connection" }
            throw failure
        }

        logger.info { message = "Connected" }
        _state.value = State.Connected(taskScope)
        return taskScope
    }

    override suspend fun connect(): CoroutineScope =
        connectAction.awaitConnect()

    override suspend fun disconnect() {
        logger.verbose { message = "Disconnect request" }
        connectAction.cancelAndJoin(CancellationException(NotConnectedException("Disconnect requested")))
    }

    override suspend fun maximumWriteValueLengthForType(writeType: WriteType): Int =
        DEFAULT_ATT_MTU - ATT_MTU_HEADER_SIZE

    @ExperimentalApi
    override suspend fun rssi(): Int = withContext(Dispatchers.IO) {
        ffi.properties().rssi?.toInt() ?: Int.MIN_VALUE
    }

    override suspend fun read(characteristic: Characteristic): ByteArray {
        logger.verbose { message = "Reading from $characteristic" }
        return withContext(Dispatchers.IO) {
            ffi.read(getCharacteristic(characteristic))
        }
    }

    override suspend fun read(descriptor: Descriptor): ByteArray {
        logger.verbose { message = "Reading from $descriptor" }
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
        logger.verbose { message = "Writing to $characteristic, type=$writeType data=${data.size} bytes" }
        return withContext(Dispatchers.IO) {
            ffi.write(getCharacteristic(characteristic), data, writeType.ffi())
        }
    }

    override suspend fun write(descriptor: Descriptor, data: ByteArray) {
        logger.verbose { message = "Writing to $descriptor, data=${data.size} bytes" }
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
        logger.debug { message = "Closing" }
        scope.cancel("$this closed")
        ffi.destroy()
    }

    override fun toString(): String = "Peripheral(identifier=$identifier)"
}

private fun WriteType.ffi() = when (this) {
    WriteType.WithResponse -> com.juul.kable.btleplug.ffi.WriteType.WITH_RESPONSE
    WriteType.WithoutResponse -> com.juul.kable.btleplug.ffi.WriteType.WITHOUT_RESPONSE
}
