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
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import kotlinx.atomicfu.atomic
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
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBService
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.native.concurrent.freeze

internal fun CoroutineScope.peripheral(
    centralManager: CentralManager,
    cbPeripheral: CBPeripheral,
) = Peripheral(coroutineContext, centralManager, cbPeripheral)

@OptIn(ExperimentalStdlibApi::class) // for CancellationException in @Throws
public actual class Peripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val centralManager: CentralManager,
    private val cbPeripheral: CBPeripheral,
) {

    private val delegate = PeripheralDelegate().freeze()

    private val scope =
        CoroutineScope(parentCoroutineContext + Job(parentCoroutineContext[Job]))

    public actual val state: Flow<State> = centralManager.delegate
        .connection
        .filter { event -> event.identifier == cbPeripheral.identifier }
        .map { event ->
            when (event) {
                is DidConnect -> State.Connected
                is DidFailToConnect -> State.Disconnected(event.error)
                is DidDisconnect -> State.Disconnected(null)
            }
        }

    // fixme: Use MutableSharedFlow; a buffer of 1 here will **not** exhibit the desired behavior.
    // We want the connection handling control flow to pause until event has been processed (rendezvous style). With a
    // buffer of 1, we won't wait (for example) for a Connected event to be processed before proceeding. So it doesn't
    // give a library consumer the ability to do connection setup (e.g. discover services) via the Connected event prior
    // to a Connected state being emitted.
    private val _events = BroadcastChannel<Event>(1)
    public actual val events: Flow<Event> = _events.asFlow()

    internal val platformServices: List<PlatformService>? = null

    // todo: Is CBPeripheral.services `null` until service discovery?
    public actual val services: List<DiscoveredService>?
        get() = cbPeripheral.services?.map { service ->
            service as CBService
            service.toPlatformService().toDiscoveredService()
        }

    private val _characteristicChange = BroadcastChannel<CharacteristicChange>(BUFFERED)

    private val wasConnected = atomic(false)

    public actual suspend fun connect(): Unit {
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

                _events.send(Event.Disconnected(wasConnected.value))
                wasConnected.value = false
            }
        }

        try {
            centralManager.connectPeripheral(cbPeripheral, delegate, options = null)

            // fixme: Handle centralManager:didFailToConnectPeripheral:error:
            // https://developer.apple.com/documentation/corebluetooth/cbcentralmanagerdelegate/1518988-centralmanager
            suspendUntilConnected()

            wasConnected.value = true
            _events.send(Event.Connected(this))
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

    public actual suspend fun disconnect(): Unit {
        wasConnected.value = false
        scope.coroutineContext[Job]?.cancelAndJoinChildren()
    }

    @Throws(CancellationException::class, IOException::class)
    public actual suspend fun rssi(): Int = mutex.withLock {
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
    public actual suspend fun write(
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
    public actual suspend fun read(
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
    public actual suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ): Unit = performAction {
        TODO("Not yet implemented")
    }

    // todo: readAsNSData
    @Throws(CancellationException::class, IOException::class)
    public actual suspend fun read(
        descriptor: Descriptor,
    ): ByteArray = mutex.withLock {
        TODO("Not yet implemented")
    }

    public actual fun observe(
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

internal fun Peripheral.cbCharacteristicFrom(
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

private fun Peripheral.cbDescriptorFrom(
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
    if (error != null) throw IOException(error.description)
    return this as T
}

private fun DidUpdateValueForCharacteristic.getOrThrow(): NSData = when (this) {
    is Data -> data
    is Error -> throw IOException(error.description)
}
