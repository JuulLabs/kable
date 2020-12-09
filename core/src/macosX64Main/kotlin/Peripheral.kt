package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.CentralManagerDelegate.ConnectionEvent
import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidConnect
import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidDisconnect
import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidFailToConnect
import com.juul.kable.PeripheralDelegate.DidUpdateValueForCharacteristic
import com.juul.kable.PeripheralDelegate.DidUpdateValueForCharacteristic.Closed
import com.juul.kable.PeripheralDelegate.DidUpdateValueForCharacteristic.Data
import com.juul.kable.PeripheralDelegate.DidUpdateValueForCharacteristic.Error
import com.juul.kable.PeripheralDelegate.Response.DidDiscoverCharacteristicsForService
import com.juul.kable.PeripheralDelegate.Response.DidDiscoverServices
import com.juul.kable.PeripheralDelegate.Response.DidReadRssi
import com.juul.kable.PeripheralDelegate.Response.DidUpdateNotificationStateForCharacteristic
import com.juul.kable.PeripheralDelegate.Response.DidUpdateValueForDescriptor
import com.juul.kable.PeripheralDelegate.Response.DidWriteValueForCharacteristic
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
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.updateAndGet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withPermit
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

public actual fun CoroutineScope.peripheral(
    advertisement: Advertisement,
): Peripheral = ApplePeripheral(coroutineContext, advertisement.cbPeripheral)

@OptIn(ExperimentalStdlibApi::class) // for CancellationException in @Throws
public class ApplePeripheral internal constructor(
    parentCoroutineContext: CoroutineContext,
    private val cbPeripheral: CBPeripheral,
) : Peripheral {

    private val job = Job(parentCoroutineContext.job) // todo: Disconnect/dispose CBPeripheral on completion?
    private val scope = CoroutineScope(parentCoroutineContext + job)

    private val centralManager: CentralManager = CentralManager.Default

    public override val state: Flow<State> = centralManager.delegate
        .connectionState
        .filter { event -> event.identifier == cbPeripheral.identifier }
        .map { event -> event.toState() }

    private val observers = Observers(this)

    internal val platformServices: List<PlatformService>?
        get() = cbPeripheral.services?.map { service ->
            service as CBService
            service.toPlatformService()
        }

    public override val services: List<DiscoveredService>?
        get() = platformServices?.map { it.toDiscoveredService() }

    private val _connection = atomic<Connection?>(null)
    private val connection: Connection
        inline get() = _connection.value ?: throw NotReadyException(toString())

    private val connectJob = atomic<Job?>(null)

    private val _ready = MutableStateFlow(false)
    internal suspend fun suspendUntilReady() {
        combine(_ready, state) { ready, state -> ready && state == State.Connected }.first { it }
    }

    private fun onDisconnected() {
        connectJob.value?.cancel()
        connectJob.value = null
        _connection.value?.close()
        _connection.value = null
    }

    private fun createConnectJob(): Job = scope.launch(start = LAZY) {
        _ready.value = false

        centralManager.delegate.onDisconnected.onEach { identifier ->
            if (identifier == cbPeripheral.identifier) onDisconnected()
        }.launchIn(scope)

        try {
            val delegate = PeripheralDelegate().freeze() // todo: Create in `connectPeripheral`.
            val connection = centralManager.connectPeripheral(cbPeripheral, delegate).also {
                _connection.value = it
            }

            connection
                .delegate
                .characteristicChanges
                .takeWhile { it !== Closed }
                .mapNotNull { it as? Data }
                .onEach(observers.characteristicChanges::emit)
                .launchIn(scope, start = UNDISPATCHED)

            // fixme: Handle centralManager:didFailToConnectPeripheral:error:
            // https://developer.apple.com/documentation/corebluetooth/cbcentralmanagerdelegate/1518988-centralmanager
            suspendUntilConnected()

            discoverServices()
            observers.rewire()
        } catch (t: Throwable) {
            withContext(NonCancellable) {
                centralManager.cancelPeripheralConnection(cbPeripheral)
                _connection.value = null
            }
            throw t
        }

        _ready.value = true
    }

    public override suspend fun connect() {
        check(job.isNotCancelled) { "Cannot connect, scope is cancelled for $this" }
        connectJob.updateAndGet { it ?: createConnectJob() }!!.join()
    }

    public override suspend fun disconnect() {
        try {
            scope.coroutineContext.job.cancelAndJoinChildren()
        } finally {
            withContext(NonCancellable) {
                centralManager.cancelPeripheralConnection(cbPeripheral)
            }
            onDisconnected()
        }
    }

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public override suspend fun rssi(): Int = connection.execute<DidReadRssi> {
        centralManager.readRssi(cbPeripheral)
    }.rssi.intValue

    private suspend fun discoverServices(): Unit = discoverServices(services = null)

    /** @param services to discover (list of service UUIDs), or `null` for all. */
    private suspend fun discoverServices(
        services: List<Uuid>?,
    ) {
        val servicesToDiscover = services?.map { CBUUID.UUIDWithNSUUID(it.toNSUUID()) }

        connection.execute<DidDiscoverServices> {
            centralManager.discoverServices(cbPeripheral, servicesToDiscover)
        }

        cbPeripheral.services?.forEach { cbService ->
            connection.execute<DidDiscoverCharacteristicsForService> {
                centralManager.discoverCharacteristics(cbPeripheral, cbService as CBService)
            }
        }
    }

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ): Unit = write(characteristic, data.toNSData(), writeType)

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun write(
        characteristic: Characteristic,
        data: NSData,
        writeType: WriteType,
    ) {
        val cbCharacteristic = cbCharacteristicFrom(characteristic)
        connection.execute<DidWriteValueForCharacteristic> {
            centralManager.write(cbPeripheral, data, cbCharacteristic, writeType.cbWriteType)
        }
    }

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public override suspend fun read(
        characteristic: Characteristic,
    ): ByteArray = readAsNSData(characteristic).toByteArray()

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun readAsNSData(
        characteristic: Characteristic,
    ): NSData {
        val connection = this.connection
        val cbCharacteristic = cbCharacteristicFrom(characteristic)

        return connection.semaphore.withPermit {
            connection
                .delegate
                .characteristicChanges
                .onSubscription { centralManager.read(cbPeripheral, cbCharacteristic) }
                .firstOrThrow { it.cbCharacteristic.UUID == cbCharacteristic.UUID }
        }
    }

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public override suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ): Unit = write(descriptor, data.toNSData())

    @Throws(CancellationException::class, IOException::class)
    public suspend fun write(
        descriptor: Descriptor,
        data: NSData,
    ) {
        val cbDescriptor = cbDescriptorFrom(descriptor)
        connection.execute<DidUpdateValueForDescriptor> {
            centralManager.write(cbPeripheral, data, cbDescriptor)
        }
    }

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public override suspend fun read(
        descriptor: Descriptor,
    ): ByteArray = readAsNSData(descriptor).toByteArray()

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun readAsNSData(
        descriptor: Descriptor,
    ): NSData {
        val cbDescriptor = cbDescriptorFrom(descriptor)
        return connection.execute<DidUpdateValueForDescriptor> {
            centralManager.read(cbPeripheral, cbDescriptor)
        }.descriptor.value as NSData
    }

    public override fun observe(
        characteristic: Characteristic
    ): Flow<ByteArray> = observeAsNSData(characteristic).map { it.toByteArray() }

    public fun observeAsNSData(
        characteristic: Characteristic
    ): Flow<NSData> = observers.acquire(characteristic)

    internal suspend fun startNotifications(characteristic: Characteristic) {
        val cbCharacteristic = cbCharacteristicFrom(characteristic)
        connection.execute<DidUpdateNotificationStateForCharacteristic> {
            centralManager.notify(cbPeripheral, cbCharacteristic)
        }
    }

    internal suspend fun stopNotifications(characteristic: Characteristic) {
        val cbCharacteristic = cbCharacteristicFrom(characteristic)
        connection.execute<DidUpdateNotificationStateForCharacteristic> {
            centralManager.cancelNotify(cbPeripheral, cbCharacteristic)
        }
    }

    override fun toString(): String = "Peripheral(cbPeripheral=$cbPeripheral)"
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

private suspend fun Peripheral.suspendUntilConnected() {
    state.first { it == State.Connected }
}

private val WriteType.cbWriteType: CBCharacteristicWriteType
    get() = when (this) {
        WithResponse -> CBCharacteristicWriteWithResponse
        WithoutResponse -> CBCharacteristicWriteWithoutResponse
    }

private suspend fun Flow<DidUpdateValueForCharacteristic>.firstOrThrow(
    predicate: suspend (Data) -> Boolean
): NSData = when (val value = first { it !is Data || predicate.invoke(it) }) {
    is Data -> value.data
    is Error -> throw IOException(value.error.description, cause = null)
    Closed -> throw ConnectionLostException()
}

private fun ConnectionEvent.toState(): State = when (this) {
    is DidConnect -> State.Connected
    is DidFailToConnect -> State.Disconnected(error?.toStatus())
    is DidDisconnect -> State.Disconnected(error?.toStatus())
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
