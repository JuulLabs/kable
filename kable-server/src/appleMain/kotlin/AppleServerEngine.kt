package com.juul.kable.server

import com.juul.kable.server.logs.Logger
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import platform.CoreBluetooth.CBAdvertisementDataLocalNameKey
import platform.CoreBluetooth.CBAdvertisementDataServiceUUIDsKey
import platform.CoreBluetooth.CBManagerState
import platform.CoreBluetooth.CBManagerStatePoweredOff
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBManagerStateResetting
import platform.CoreBluetooth.CBManagerStateUnauthorized
import platform.CoreBluetooth.CBManagerStateUnknown
import platform.CoreBluetooth.CBManagerStateUnsupported
import platform.CoreBluetooth.CBMutableCharacteristic
import platform.CoreBluetooth.CBPeripheralManager

internal class AppleServerEngine(
    private val logger: Logger,
) : ServerEngine {

    private val dispatcher = QueueDispatcher("com.juul.kable.server")

    // Guarded by `guard`.
    private var manager: CBPeripheralManager? = null
    private var delegate: PeripheralManagerDelegate? = null
    private var characteristics = emptyMap<AttributeKey.Characteristic, CBMutableCharacteristic>()

    private val guard = Mutex()

    /** Serializes notifications (for simpler `onReadyToUpdateSubscribers` backpressure handling). */
    private val notifying = Mutex()

    override suspend fun open(profile: ServerProfile): ReceiveChannel<InboundRequest> = guard.withLock {
        check(manager == null) { "GATT server already open" }

        val delegate = PeripheralManagerDelegate(logger)
        val manager = CBPeripheralManager(delegate, dispatcher.dispatchQueue)
        this.delegate = delegate
        this.manager = manager

        try {
            val state = delegate.state.first { it != CBManagerStateUnknown && it != CBManagerStateResetting }
            check(state == CBManagerStatePoweredOn) {
                "Unable to open GATT server, bluetooth state is ${state.nameString}"
            }

            val registry = mutableMapOf<AttributeKey.Characteristic, CBMutableCharacteristic>()
            profile.services.forEach { service ->
                val (cbService, cbCharacteristics) = service.toCBMutableService(logger)
                registry += cbCharacteristics

                // `addService` must not be called again until `didAddService` is received.
                withContext(dispatcher) { manager.addService(cbService) }
                val error = delegate.onServiceAdded.receive()
                check(error == null) { "Failed to add service ${service.uuid}: ${error?.localizedDescription}" }
            }
            characteristics = registry.toMap()
        } catch (e: Exception) {
            closeManager()
            throw e
        }

        delegate.requests
    }

    override suspend fun close(): Unit = guard.withLock {
        closeManager()
    }

    private suspend fun closeManager() {
        delegate?.close()
        manager?.let { manager ->
            withContext(NonCancellable) {
                withContext(dispatcher) {
                    if (manager.isAdvertising) manager.stopAdvertising()
                    manager.removeAllServices()
                    manager.delegate = null
                }
            }
        }
        manager = null
        delegate = null
        characteristics = emptyMap()
    }

    override suspend fun notify(central: Central, characteristic: ServerCharacteristic, value: ByteArray) {
        // `manager`/`delegate`/`characteristics` are read without `guard` (which would deadlock
        // `notify` against `close`, as `close` cancels-and-joins notifying coroutines).
        val manager = checkNotNull(manager) { "GATT server not open" }
        val delegate = checkNotNull(delegate) { "GATT server not open" }
        val cbCharacteristic = characteristics.getValue(
            AttributeKey.Characteristic(characteristic.serviceUuid, characteristic.characteristicUuid),
        )
        val cbCentral = (central as AppleCentral).cbCentral

        notifying.withLock {
            logger.debug {
                "updateValue central=${central.identifier} " +
                    "uuid=${characteristic.characteristicUuid}" + logger.data(value)
            }
            val data = value.toNSData()
            while (true) {
                // Clear (any stale) readiness signal before attempting `updateValue`, so that (if
                // needed) the subsequent `receive` suspends until the queue has space again.
                delegate.onReadyToUpdateSubscribers.tryReceive()
                val sent = withContext(dispatcher) {
                    manager.updateValue(data, cbCharacteristic, listOf(cbCentral))
                }
                if (sent) break
                // Underlying queue is full: apply backpressure by suspending until
                // `peripheralManagerIsReadyToUpdateSubscribers` is received.
                val result = delegate.onReadyToUpdateSubscribers.receiveCatching()
                if (result.isClosed) throw IOException("GATT server was closed while sending notification")
            }
        }
    }

    override suspend fun advertise(parameters: AdvertisementParameters): Nothing {
        val manager = checkNotNull(manager) { "GATT server not open" }
        val delegate = checkNotNull(delegate) { "GATT server not open" }

        val advertisementData = buildMap<Any?, Any> {
            parameters.name?.let { name -> put(CBAdvertisementDataLocalNameKey, name) }
            if (parameters.services.isNotEmpty()) {
                put(CBAdvertisementDataServiceUUIDsKey, parameters.services.map { it.toCBUUID() })
            }
        }

        try {
            // Clear (any stale) result of a previous `startAdvertising` invocation.
            delegate.onAdvertisingStarted.tryReceive()
            withContext(dispatcher) {
                manager.startAdvertising(advertisementData.ifEmpty { null })
            }
            val error = delegate.onAdvertisingStarted.receive()
            if (error != null) {
                throw AdvertiseException("Failed to start advertising: ${error.localizedDescription}")
            }
            logger.debug { "Advertising started" }
            awaitCancellation()
        } finally {
            withContext(NonCancellable) {
                withContext(dispatcher) {
                    if (manager.isAdvertising) manager.stopAdvertising()
                }
                logger.debug { "Advertising stopped" }
            }
        }
    }
}

private val CBManagerState.nameString: String
    get() = when (this) {
        CBManagerStateUnknown -> "Unknown"
        CBManagerStateResetting -> "Resetting"
        CBManagerStateUnsupported -> "Unsupported"
        CBManagerStateUnauthorized -> "Unauthorized"
        CBManagerStatePoweredOff -> "PoweredOff"
        CBManagerStatePoweredOn -> "PoweredOn"
        else -> "Unknown($this)"
    }
