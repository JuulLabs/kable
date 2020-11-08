package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidConnect
import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidDisconnect
import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidFailToConnect
import com.juul.kable.PeripheralDelegate.DidUpdateValueForCharacteristic
import com.juul.kable.PeripheralDelegate.DidUpdateValueForCharacteristic.Data
import com.juul.kable.PeripheralDelegate.DidUpdateValueForCharacteristic.Error
import com.juul.kable.PeripheralDelegate.Response
import com.juul.kable.PeripheralDelegate.Response.DidReadRssi
import com.juul.kable.State.Disconnected.Status.Cancelled
import com.juul.kable.State.Disconnected.Status.ConnectionLimitReached
import com.juul.kable.State.Disconnected.Status.EncryptionTimedOut
import com.juul.kable.State.Disconnected.Status.Failed
import com.juul.kable.State.Disconnected.Status.PeripheralDisconnected
import com.juul.kable.State.Disconnected.Status.Timeout
import com.juul.kable.State.Disconnected.Status.Unknown
import com.juul.kable.State.Disconnected.Status.UnknownDevice
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBCharacteristicWriteType
import platform.CoreBluetooth.CBCharacteristicWriteWithResponse
import platform.CoreBluetooth.CBCharacteristicWriteWithoutResponse
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBErrorConnectionFailed
import platform.CoreBluetooth.CBErrorConnectionLimitReached
import platform.CoreBluetooth.CBErrorConnectionTimeout
import platform.CoreBluetooth.CBErrorEncryptionTimedOut
import platform.CoreBluetooth.CBErrorOperationCancelled
import platform.CoreBluetooth.CBErrorPeripheralDisconnected
import platform.CoreBluetooth.CBErrorUnknownDevice
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBService
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData
import platform.Foundation.NSError
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.native.concurrent.freeze

internal fun CoroutineScope.peripheral(
    centralManager: CentralManager,
    cbPeripheral: CBPeripheral,
) = ApplePeripheral(coroutineContext, centralManager, cbPeripheral)

@OptIn(ExperimentalStdlibApi::class) // for CancellationException in @Throws
public class ApplePeripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val centralManager: CentralManager,
    private val cbPeripheral: CBPeripheral,
) : Peripheral {

    private val delegate = PeripheralDelegate().freeze()

    private val scope =
        CoroutineScope(parentCoroutineContext + Job(parentCoroutineContext[Job]))

    public override val state: Flow<State> = centralManager.delegate
        .connection
        .filter { event -> event.identifier == cbPeripheral.identifier }
        .map { event ->
            when (event) {
                is DidConnect -> State.Connected
                is DidFailToConnect -> State.Disconnected(event.error?.toStatus())
                is DidDisconnect -> State.Disconnected(event.error?.toStatus())
            }
        }

    // fixme: Use MutableSharedFlow.
    private val _events = BroadcastChannel<Event>(1)
    public override val events: Flow<Event> = _events.asFlow()

    internal val platformServices: List<PlatformService>? = null

    // todo: Is CBPeripheral.services `null` until service discovery?
    public override val services: List<DiscoveredService>?
        get() = cbPeripheral.services?.map { service ->
            service as CBService
            service.toPlatformService().toDiscoveredService()
        }

    private val _characteristicChange = BroadcastChannel<CharacteristicChange>(BUFFERED)

    public override suspend fun connect(): Unit {
        val job = scope.launch(start = UNDISPATCHED) {
            try {
                // todo: Restore observations.
                delegate.characteristicChange.collect(::onCharacteristicChange)
            } finally {
                println("connect finally")
                withContext(NonCancellable) {
                    println("connect cancel")
                    centralManager.cancelPeripheralConnection(cbPeripheral)
                }

                _events.send(Event.Disconnected)
            }
        }

        try {
            centralManager.connectPeripheral(cbPeripheral, delegate, options = null)

            // fixme: Handle centralManager:didFailToConnectPeripheral:error:
            // https://developer.apple.com/documentation/corebluetooth/cbcentralmanagerdelegate/1518988-centralmanager
            suspendUntilConnected()

            discoverServices()
            // todo: Re-wire observers.

            _events.send(Event.Ready)
        } catch (cancellation: CancellationException) {
            job.cancel()
            throw cancellation
        }
    }

    private suspend fun onCharacteristicChange(change: DidUpdateValueForCharacteristic) {
        if (change is Data) {
            val characteristic = Characteristic(change.cbCharacteristic)
            _characteristicChange.send(CharacteristicChange(characteristic, change.data))
        }
    }

    public override suspend fun disconnect(): Unit {
        scope.coroutineContext[Job]?.cancelAndJoinChildren()
    }

    @Throws(CancellationException::class, IOException::class)
    public override suspend fun rssi(): Int = mutex.withLock {
        centralManager.readRssi(cbPeripheral)
        delegate.response
            .receive()
            .getOrThrow<DidReadRssi>()
            .rssi
            .intValue
    }

    @Throws(CancellationException::class, IOException::class)
    private suspend fun discoverServices(): Unit = discoverServices(services = null)

    /** @param services to discover (list of service UUIDs), or `null` for all. */
    @Throws(CancellationException::class, IOException::class)
    public suspend fun discoverServices(
        services: List<Uuid>?,
    ): Unit {
        val servicesToDiscover = services?.map { CBUUID.UUIDWithNSUUID(it.toNSUUID()) }
        performAction {
            centralManager.discoverServices(
                cbPeripheral,
                servicesToDiscover
            )
        }

        val cbServices = this.services?.map { it.cbService } ?: emptyList()
        cbServices.forEach { cbService ->
            performAction {
                centralManager.discoverCharacteristics(cbPeripheral, cbService)
            }
        }
    }

    @Throws(CancellationException::class, IOException::class)
    public override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ): Unit = write(characteristic, data.toNSData(), writeType)

    @Throws(CancellationException::class, IOException::class)
    public suspend fun write(
        characteristic: Characteristic,
        data: NSData,
        writeType: WriteType,
    ): Unit = performAction {
        centralManager.write(
            cbPeripheral,
            data,
            characteristic.cbCharacteristic,
            writeType.cbWriteType,
        )
    }

    @Throws(CancellationException::class, IOException::class)
    public override suspend fun read(
        characteristic: Characteristic,
    ): ByteArray = readAsNSData(characteristic).toByteArray()

    @Throws(CancellationException::class, IOException::class)
    public suspend fun readAsNSData(
        characteristic: Characteristic,
    ): NSData = mutex.withLock {
        centralManager.read(cbPeripheral, characteristic.cbCharacteristic)
        return delegate.characteristicChange
            .first { it.cbCharacteristic.UUID == characteristic.cbCharacteristic.UUID }
            .getOrThrow()
    }

    @Throws(CancellationException::class, IOException::class)
    public override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ): Unit = performAction {
        TODO("Not yet implemented")
    }

    // todo: readAsNSData
    @Throws(CancellationException::class, IOException::class)
    public override suspend fun read(
        descriptor: Descriptor,
    ): ByteArray = mutex.withLock {
        TODO("Not yet implemented")
    }

    public override fun observe(
        characteristic: Characteristic
    ): Flow<ByteArray> = observeAsNSData(characteristic).map { it.toByteArray() }

    public fun observeAsNSData(
        characteristic: Characteristic
    ): Flow<NSData> = delegate.characteristicChange
        .onStart {
            centralManager.notify(cbPeripheral, characteristic.cbCharacteristic)
        }
        .onCompletion {
            centralManager.cancelNotify(cbPeripheral, characteristic.cbCharacteristic)
        }
        .filter { it.cbCharacteristic.UUID == characteristic.cbCharacteristic.UUID }
        .mapNotNull { (it as? Data)?.data }

    private val mutex = Mutex()

    private suspend inline fun performAction(
        action: () -> Unit
    ): Unit = mutex.withLock {
        action.invoke()
        val response = delegate.response.receive()
        val error = response.error
        if (error != null) throw IOException(error.description)
    }
}

internal fun ApplePeripheral.cbCharacteristicFrom(
    characteristic: Characteristic,
): CBCharacteristic {
    val services = checkNotNull(platformServices) {
        "Services have not been discovered for $this"
    }
    val characteristics = services
        .first { it.serviceUuid == characteristic.serviceUuid }
        .characteristics
    return characteristics
        .first { it.characteristicUuid == characteristic.characteristicUuid }
        .cbCharacteristic
}

private fun ApplePeripheral.cbDescriptorFrom(
    descriptor: Descriptor,
): CBDescriptor {
    val services = checkNotNull(platformServices) {
        "Services have not been discovered for $this"
    }
    val characteristics = services
        .first { it.serviceUuid == descriptor.serviceUuid }
        .characteristics
    val descriptors = characteristics
        .first { it.characteristicUuid == descriptor.characteristicUuid }
        .descriptors
    return descriptors
        .first { it.descriptorUuid == descriptor.descriptorUuid }
        .cbDescriptor
}

private data class CharacteristicChange(
    val characteristic: Characteristic,
    val data: NSData
)

private suspend fun Peripheral.suspendUntilConnected(): Unit {
    state.first { it == State.Connected }
}

private val WriteType.cbWriteType: CBCharacteristicWriteType
    get() = when (this) {
        WithResponse -> CBCharacteristicWriteWithResponse
        WithoutResponse -> CBCharacteristicWriteWithoutResponse
    }

private fun <T> Response.getOrThrow(): T {
    val error = this.error
    if (error != null) throw IOException(error.description, cause = null)
    return this as T
}

private fun DidUpdateValueForCharacteristic.getOrThrow(): NSData = when (this) {
    is Data -> data
    is Error -> throw IOException(error.description, cause = null)
}

private fun NSError.toStatus(): State.Disconnected.Status = when (code) {
    CBErrorPeripheralDisconnected -> PeripheralDisconnected
    CBErrorConnectionFailed -> Failed
    CBErrorConnectionTimeout -> Timeout
    CBErrorUnknownDevice -> UnknownDevice
    CBErrorOperationCancelled -> Cancelled
    CBErrorConnectionLimitReached -> ConnectionLimitReached
    CBErrorEncryptionTimedOut -> EncryptionTimedOut
    else -> Unknown(code.toInt())
}
