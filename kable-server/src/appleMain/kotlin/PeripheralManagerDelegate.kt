package com.juul.kable.server

import com.juul.kable.server.logs.Logger
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreBluetooth.CBATTErrorSuccess
import platform.CoreBluetooth.CBATTRequest
import platform.CoreBluetooth.CBCentral
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBManagerState
import platform.CoreBluetooth.CBManagerStateUnknown
import platform.CoreBluetooth.CBPeripheralManager
import platform.CoreBluetooth.CBPeripheralManagerDelegateProtocol
import platform.CoreBluetooth.CBService
import platform.Foundation.NSError
import platform.darwin.NSObject

// https://developer.apple.com/documentation/corebluetooth/cbperipheralmanagerdelegate
internal class PeripheralManagerDelegate(
    private val logger: Logger,
) : NSObject(), CBPeripheralManagerDelegateProtocol {

    private val _state = MutableStateFlow(CBManagerStateUnknown)
    val state: StateFlow<CBManagerState> = _state.asStateFlow()

    private val _requests = Channel<InboundRequest>(UNLIMITED)
    val requests: ReceiveChannel<InboundRequest> = _requests

    /** `null` on success (error otherwise) of the most recent `addService` call. */
    val onServiceAdded = Channel<NSError?>(UNLIMITED)

    /** `null` on success (error otherwise) of the most recent `startAdvertising` call. */
    val onAdvertisingStarted = Channel<NSError?>(CONFLATED)

    /** Signals `peripheralManagerIsReadyToUpdateSubscribers` (following a failed `updateValue`). */
    val onReadyToUpdateSubscribers = Channel<Unit>(CONFLATED)

    fun close() {
        _requests.close()
    }

    /* Monitoring the Peripheral Manager's State */

    override fun peripheralManagerDidUpdateState(peripheral: CBPeripheralManager) {
        logger.debug { "peripheralManagerDidUpdateState state=${peripheral.state}" }
        _state.value = peripheral.state
    }

    override fun peripheralManager(peripheral: CBPeripheralManager, willRestoreState: Map<Any?, *>) {
        // No-op.
    }

    /* Adding Services */

    override fun peripheralManager(
        peripheral: CBPeripheralManager,
        didAddService: CBService,
        error: NSError?,
    ) {
        logger.debug { "didAddService uuid=${didAddService.UUID.UUIDString} error=$error" }
        onServiceAdded.trySendOrLog(error)
    }

    /* Advertising Peripheral Data */

    override fun peripheralManagerDidStartAdvertising(peripheral: CBPeripheralManager, error: NSError?) {
        logger.debug { "peripheralManagerDidStartAdvertising error=$error" }
        onAdvertisingStarted.trySendOrLog(error)
    }

    /* Monitoring Subscriptions to Characteristic Values */

    @ObjCSignatureOverride
    override fun peripheralManager(
        peripheral: CBPeripheralManager,
        central: CBCentral,
        didSubscribeToCharacteristic: CBCharacteristic,
    ) {
        logger.debug {
            "didSubscribeToCharacteristic central=${central.identifier.UUIDString} " +
                "uuid=${didSubscribeToCharacteristic.UUID.UUIDString}"
        }
        val attribute = didSubscribeToCharacteristic.attributeKeyOrNull() ?: return
        _requests.trySendOrLog(InboundRequest.Subscribe(AppleCentral(central), attribute))
    }

    @ObjCSignatureOverride
    override fun peripheralManager(
        peripheral: CBPeripheralManager,
        central: CBCentral,
        didUnsubscribeFromCharacteristic: CBCharacteristic,
    ) {
        logger.debug {
            "didUnsubscribeFromCharacteristic central=${central.identifier.UUIDString} " +
                "uuid=${didUnsubscribeFromCharacteristic.UUID.UUIDString}"
        }
        val attribute = didUnsubscribeFromCharacteristic.attributeKeyOrNull() ?: return
        _requests.trySendOrLog(InboundRequest.Unsubscribe(AppleCentral(central), attribute))
    }

    override fun peripheralManagerIsReadyToUpdateSubscribers(peripheral: CBPeripheralManager) {
        logger.debug { "peripheralManagerIsReadyToUpdateSubscribers" }
        onReadyToUpdateSubscribers.trySend(Unit)
    }

    /* Receiving Read and Write Requests */

    override fun peripheralManager(peripheral: CBPeripheralManager, didReceiveReadRequest: CBATTRequest) {
        val request = didReceiveReadRequest
        logger.debug {
            "didReceiveReadRequest central=${request.central.identifier.UUIDString} " +
                "uuid=${request.characteristic.UUID.UUIDString} offset=${request.offset}"
        }
        val attribute = request.characteristic.attributeKeyOrNull()
        if (attribute == null) {
            peripheral.respondToRequest(request, AttError.InvalidHandle.cbAttError)
            return
        }
        _requests.trySendOrLog(
            InboundRequest.Read(
                central = AppleCentral(request.central),
                attribute = attribute,
                offset = request.offset.toInt(),
                respond = { value ->
                    request.value = value.toNSData()
                    peripheral.respondToRequest(request, CBATTErrorSuccess)
                },
                fail = { error -> peripheral.respondToRequest(request, error.cbAttError) },
            ),
        )
    }

    override fun peripheralManager(peripheral: CBPeripheralManager, didReceiveWriteRequests: List<*>) {
        val requests = didReceiveWriteRequests.filterIsInstance<CBATTRequest>()
        if (requests.isEmpty()) return
        val first = requests.first()
        logger.debug {
            "didReceiveWriteRequests central=${first.central.identifier.UUIDString} count=${requests.size}"
        }

        // Per Apple documentation: "Treat multiple requests as you would a single request — if any
        // individual request cannot be fulfilled, you should not fulfill any of them" and "respond
        // ... to the first request".
        // https://developer.apple.com/documentation/corebluetooth/cbperipheralmanagerdelegate/peripheralmanager(_:didreceivewrite:)
        val central = AppleCentral(first.central)
        if (requests.size == 1 && first.offset.toInt() == 0) {
            val attribute = first.characteristic.attributeKeyOrNull()
            if (attribute == null) {
                peripheral.respondToRequest(first, AttError.InvalidHandle.cbAttError)
                return
            }
            _requests.trySendOrLog(
                InboundRequest.Write(
                    central = central,
                    attribute = attribute,
                    value = first.value?.toByteArray() ?: ByteArray(0),
                    offset = 0,
                    prepared = false,
                    respond = { peripheral.respondToRequest(first, CBATTErrorSuccess) },
                    fail = { error -> peripheral.respondToRequest(first, error.cbAttError) },
                ),
            )
        } else {
            // Long (or multi-attribute) write transaction: queue fragments then (atomically)
            // commit, with the (single) response tied to the commit.
            //
            // Note: fragments and commit are enqueued together (within this single callback), so
            // queued fragments can never be left dangling (Core Bluetooth assembles prepared-write
            // queues itself and only delivers complete — never aborted — transactions to the app).
            requests.forEach { request ->
                val attribute = request.characteristic.attributeKeyOrNull()
                if (attribute == null) {
                    peripheral.respondToRequest(first, AttError.InvalidHandle.cbAttError)
                    return
                }
                _requests.trySendOrLog(
                    InboundRequest.Write(
                        central = central,
                        attribute = attribute,
                        value = request.value?.toByteArray() ?: ByteArray(0),
                        offset = request.offset.toInt(),
                        prepared = true,
                        respond = null,
                        fail = null,
                    ),
                )
            }
            _requests.trySendOrLog(
                InboundRequest.ExecuteWrite(
                    central = central,
                    commit = true,
                    respond = { peripheral.respondToRequest(first, CBATTErrorSuccess) },
                    fail = { error -> peripheral.respondToRequest(first, error.cbAttError) },
                ),
            )
        }
    }

    private fun CBCharacteristic.attributeKeyOrNull(): AttributeKey.Characteristic? {
        val serviceUuid = service?.UUID?.toUuid()
        if (serviceUuid == null) {
            logger.warn { "Unable to identify service of characteristic ${UUID.UUIDString}" }
            return null
        }
        return AttributeKey.Characteristic(serviceUuid, UUID.toUuid())
    }

    private fun <E> SendChannel<E>.trySendOrLog(element: E) {
        trySend(element).onFailure { cause ->
            logger.warn(cause) { "Unable to deliver $element" }
        }
    }
}
