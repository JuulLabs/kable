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
import kotlinx.cinterop.ObjCSignatureOverride
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
        // todo
    }

    fun close(cause: Throwable?) {
        _response.close(NotConnectedException(cause = cause))
        characteristicChanges.emitBlocking(ObservationEvent.Disconnected)
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
