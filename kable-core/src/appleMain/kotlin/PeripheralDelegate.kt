package com.juul.kable

import com.juul.kable.PeripheralDelegate.Response.DidDiscoverServices
import com.juul.kable.PeripheralDelegate.Response.DidReadRssi
import com.juul.kable.PeripheralDelegate.Response.DidUpdateNotificationStateForCharacteristic
import com.juul.kable.PeripheralDelegate.Response.DidUpdateValueForDescriptor
import com.juul.kable.PeripheralDelegate.Response.DidWriteValueForCharacteristic
import com.juul.kable.logs.LogMessage
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.logs.Logging.DataProcessor.Operation.Change
import com.juul.kable.logs.detail
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.io.IOException
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBL2CAPChannel
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBPeripheralDelegateProtocol
import platform.CoreBluetooth.CBService
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.NSUUID
import platform.darwin.NSObject

// https://developer.apple.com/documentation/corebluetooth/cbperipheraldelegate
internal class PeripheralDelegate(
    private val canSendWriteWithoutResponse: MutableStateFlow<Boolean>,
    private val characteristicChanges: MutableSharedFlow<ObservationEvent<NSData>>,
    logging: Logging,
    internal val identifier: String,
) : NSObject(), CBPeripheralDelegateProtocol {

    sealed class Response {

        abstract val peripheralIdentifier: NSUUID
        abstract val error: NSError?

        data class DidDiscoverServices(
            override val peripheralIdentifier: NSUUID,
            override val error: NSError?,
        ) : Response()

        data class DidDiscoverCharacteristicsForService(
            override val peripheralIdentifier: NSUUID,
            val service: CBService,
            override val error: NSError?,
        ) : Response()

        data class DidDiscoverDescriptorsForCharacteristic(
            override val peripheralIdentifier: NSUUID,
            val characteristic: CBCharacteristic,
            override val error: NSError?,
        ) : Response()

        data class DidWriteValueForCharacteristic(
            override val peripheralIdentifier: NSUUID,
            val characteristic: CBCharacteristic,
            override val error: NSError?,
        ) : Response()

        data class DidUpdateValueForDescriptor(
            override val peripheralIdentifier: NSUUID,
            val descriptor: CBDescriptor,
            override val error: NSError?,
        ) : Response()

        data class DidUpdateNotificationStateForCharacteristic(
            override val peripheralIdentifier: NSUUID,
            val characteristic: CBCharacteristic,
            override val error: NSError?,
        ) : Response()

        data class DidReadRssi(
            override val peripheralIdentifier: NSUUID,
            val rssi: NSNumber,
            override val error: NSError?,
        ) : Response()
    }

    data class OnServiceChanged(
        val cbServices: List<CBService>,
    )

    private val _response = Channel<Response>(BUFFERED)
    val response: ReceiveChannel<Response> = _response

    val onServiceChanged = Channel<OnServiceChanged>(CONFLATED)

    // Single-flight state for an in-progress `openL2CAPChannel`. CoreBluetooth's `didOpenL2CAPChannel`
    // callback carries no PSM (and none at all on failure), so it cannot be correlated to a specific
    // request; the caller (Connection.openL2CapChannel) instead holds a mutex so only one open is ever
    // outstanding, and this single slot delivers that open's result. The lock guards the slot against the
    // caller thread and the CoreBluetooth callback thread racing.
    internal data class L2CapOpenResult(
        val channel: CBL2CAPChannel?,
        val error: NSError?,
    )

    private val l2CapOpenLock = SynchronizedObject()
    private var pendingL2CapOpen: CompletableDeferred<L2CapOpenResult>? = null

    internal fun beginL2CapOpen(): CompletableDeferred<L2CapOpenResult> =
        synchronized(l2CapOpenLock) {
            check(pendingL2CapOpen == null) { "An L2CAP open is already in progress" }
            CompletableDeferred<L2CapOpenResult>().also { pendingL2CapOpen = it }
        }

    internal fun cancelL2CapOpen(pending: CompletableDeferred<L2CapOpenResult>, cause: Throwable) {
        val cleared = synchronized(l2CapOpenLock) {
            (pendingL2CapOpen === pending).also { if (it) pendingL2CapOpen = null }
        }
        if (cleared) pending.completeExceptionally(cause)
    }

    private fun completeL2CapOpen(result: L2CapOpenResult): Boolean {
        val pending = synchronized(l2CapOpenLock) {
            pendingL2CapOpen.also { pendingL2CapOpen = null }
        }
        return pending?.complete(result) == true
    }

    private val logger = Logger(logging, tag = "Kable/Delegate", identifier = identifier)

    /* Discovering Services */

    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverServices: NSError?,
    ) {
        logger.debug {
            message = "didDiscoverServices"
            detail(didDiscoverServices)
        }
        _response.sendBlocking(DidDiscoverServices(peripheral.identifier, didDiscoverServices))
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverIncludedServicesForService: CBService,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "didDiscoverIncludedServicesForService"
            detail(didDiscoverIncludedServicesForService)
        }
        // todo
    }

    /* Discovering Characteristics and their Descriptors */

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverCharacteristicsForService: CBService,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "didDiscoverCharacteristicsForService"
            detail(didDiscoverCharacteristicsForService)
        }
        _response.sendBlocking(
            Response.DidDiscoverCharacteristicsForService(
                peripheral.identifier,
                didDiscoverCharacteristicsForService,
                null,
            ),
        )
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverDescriptorsForCharacteristic: CBCharacteristic,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "didDiscoverDescriptorsForCharacteristic"
            detail(didDiscoverDescriptorsForCharacteristic)
        }
        _response.sendBlocking(
            Response.DidDiscoverDescriptorsForCharacteristic(
                peripheral.identifier,
                didDiscoverDescriptorsForCharacteristic,
                null,
            ),
        )
    }

    /* Retrieving Characteristic and Descriptor Values */

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateValueForCharacteristic: CBCharacteristic,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "didUpdateValueForCharacteristic"
            detail(didUpdateValueForCharacteristic)
            detail(didUpdateValueForCharacteristic.value, Change)
        }

        val characteristic = PlatformDiscoveredCharacteristic(didUpdateValueForCharacteristic)
        val change = if (error == null) {
            // Assumption: `value == null` and `error == null` are mutually exclusive.
            // i.e. When `error == null` then `CBCharacteristic`'s `value` is non-null.
            val data = didUpdateValueForCharacteristic.value!!
            ObservationEvent.CharacteristicChange(characteristic, data)
        } else {
            ObservationEvent.Error(characteristic, IOException(error.description, cause = null))
        }

        characteristicChanges.emitBlocking(change)
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateValueForDescriptor: CBDescriptor,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "didUpdateValueForDescriptor"
            detail(didUpdateValueForDescriptor)
        }
        _response.sendBlocking(
            DidUpdateValueForDescriptor(peripheral.identifier, didUpdateValueForDescriptor, error),
        )
    }

    /* Writing Characteristic and Descriptor Values */

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didWriteValueForCharacteristic: CBCharacteristic,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "didWriteValueForCharacteristic"
            detail(didWriteValueForCharacteristic)
        }
        _response.sendBlocking(
            DidWriteValueForCharacteristic(
                peripheral.identifier,
                didWriteValueForCharacteristic,
                error,
            ),
        )
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didWriteValueForDescriptor: CBDescriptor,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "didWriteValueForDescriptor"
            detail(didWriteValueForDescriptor)
        }
        // todo
    }

    override fun peripheralIsReadyToSendWriteWithoutResponse(
        peripheral: CBPeripheral,
    ) {
        logger.debug {
            message = "peripheralIsReadyToSendWriteWithoutResponse"
        }
        canSendWriteWithoutResponse.value = true
    }

    /* Managing Notifications for a Characteristic’s Value */

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateNotificationStateForCharacteristic: CBCharacteristic,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "didUpdateNotificationStateForCharacteristic"
            detail(didUpdateNotificationStateForCharacteristic)
        }
        _response.sendBlocking(
            DidUpdateNotificationStateForCharacteristic(
                peripheral.identifier,
                didUpdateNotificationStateForCharacteristic,
                error,
            ),
        )
    }

    /* Retrieving a Peripheral’s RSSI Data */

    override fun peripheral(
        peripheral: CBPeripheral,
        didReadRSSI: NSNumber,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "didReadRSSI"
            detail("rssi", didReadRSSI.stringValue)
        }
        _response.sendBlocking(DidReadRssi(peripheral.identifier, didReadRSSI, error))
    }

    /* Monitoring Changes to a Peripheral’s Name or Services */

    override fun peripheralDidUpdateName(peripheral: CBPeripheral) {
        logger.debug {
            message = "peripheralDidUpdateName"
        }
        // todo
    }

    override fun peripheral(
        peripheral: CBPeripheral,
        didModifyServices: List<*>,
    ) {
        // Cast should be safe since `didModifyServices` type is `[CBService]`, according to:
        // https://developer.apple.com/documentation/corebluetooth/cbperipheraldelegate/peripheral(_:didmodifyservices:)
        @Suppress("UNCHECKED_CAST")
        val invalidatedServices = didModifyServices as List<CBService>

        logger.debug {
            message = "didModifyServices"
            detail("invalidatedServices", invalidatedServices.toString())
        }
        onServiceChanged.sendBlocking(OnServiceChanged(invalidatedServices))
    }

    /* Monitoring L2CAP Channels */

    override fun peripheral(
        peripheral: CBPeripheral,
        didOpenL2CAPChannel: CBL2CAPChannel?,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "didOpenL2CAPChannel"
        }
        val delivered = completeL2CapOpen(L2CapOpenResult(didOpenL2CAPChannel, error))
        if (!delivered) {
            // No waiter — the opening caller was already cancelled/disconnected. A channel handed over now
            // has no owner, so tear it down rather than leak it (its streams would otherwise stay open).
            didOpenL2CAPChannel?.let { AppleL2CapSocket(it).abandon() }
            logger.warn { message = "Discarded unexpected didOpenL2CAPChannel callback" }
            return
        }
        if (error == null && didOpenL2CAPChannel != null) {
            logger.info {
                message = "L2CAP channel open. Peer: ${didOpenL2CAPChannel.peer?.identifier}"
            }
        }
    }

    fun close(cause: Throwable?) {
        // Fail an L2CAP open still in flight when the peripheral disconnects, otherwise its caller stays
        // suspended forever — CoreBluetooth never delivers didOpenL2CAPChannel for a dropped connection.
        cancelL2CapOpenInFlight(NotConnectedException(cause = cause))
        _response.close(NotConnectedException(cause = cause))
        characteristicChanges.emitBlocking(ObservationEvent.Disconnected)
        canSendWriteWithoutResponse.value = true
    }

    private fun cancelL2CapOpenInFlight(cause: Throwable) {
        val pending = synchronized(l2CapOpenLock) {
            pendingL2CapOpen.also { pendingL2CapOpen = null }
        }
        pending?.completeExceptionally(cause)
    }
}

private inline fun Logger.debug(error: NSError?, crossinline init: LogMessage.() -> Unit) {
    if (error == null) {
        debug(init = init)
    } else {
        warn {
            init()
            detail(error)
        }
    }
}
