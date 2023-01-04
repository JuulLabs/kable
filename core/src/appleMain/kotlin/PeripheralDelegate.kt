package com.juul.kable

import com.juul.kable.PeripheralDelegate.DidUpdateValueForCharacteristic.Closed
import com.juul.kable.PeripheralDelegate.Response.DidDiscoverServices
import com.juul.kable.PeripheralDelegate.Response.DidReadRssi
import com.juul.kable.PeripheralDelegate.Response.DidUpdateNotificationStateForCharacteristic
import com.juul.kable.PeripheralDelegate.Response.DidUpdateValueForDescriptor
import com.juul.kable.PeripheralDelegate.Response.DidWriteValueForCharacteristic
import com.juul.kable.logs.LogMessage
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.logs.detail
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    logging: Logging,
    identifier: String,
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

    private val _response = Channel<Response>(BUFFERED)
    val response: ReceiveChannel<Response> = _response

    sealed class DidUpdateValueForCharacteristic {

        data class Data(
            val cbCharacteristic: CBCharacteristic,
            val data: NSData,
        ) : DidUpdateValueForCharacteristic()

        data class Error(
            val cbCharacteristic: CBCharacteristic,
            val error: NSError,
        ) : DidUpdateValueForCharacteristic()

        /** Signal to downstream that [PeripheralDelegate] has been [closed][close]. */
        object Closed : DidUpdateValueForCharacteristic()
    }

    private val _characteristicChanges = MutableSharedFlow<DidUpdateValueForCharacteristic>(extraBufferCapacity = 64)
    val characteristicChanges = _characteristicChanges.asSharedFlow()

    private val logger = Logger(logging, tag = "Kable/Delegate", identifier = identifier)

    /* Discovering Services */

    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverServices: NSError?,
    ) {
        logger.debug {
            message = "${peripheral.identifier} didDiscoverServices"
            detail(didDiscoverServices)
        }
        _response.sendBlocking(DidDiscoverServices(peripheral.identifier, didDiscoverServices))
    }

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverIncludedServicesForService: CBService,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "${peripheral.identifier} didDiscoverIncludedServicesForService"
            detail(didDiscoverIncludedServicesForService)
        }
        // todo
    }

    /* Discovering Characteristics and their Descriptors */

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverCharacteristicsForService: CBService,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "${peripheral.identifier} didDiscoverCharacteristicsForService"
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

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverDescriptorsForCharacteristic: CBCharacteristic,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "${peripheral.identifier} didDiscoverDescriptorsForCharacteristic"
            detail(didDiscoverDescriptorsForCharacteristic)
        }
        // todo
    }

    /* Retrieving Characteristic and Descriptor Values */

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateValueForCharacteristic: CBCharacteristic,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "${peripheral.identifier} didUpdateValueForCharacteristic"
            detail(didUpdateValueForCharacteristic)
            detail(didUpdateValueForCharacteristic.value)
        }

        val change = if (error == null) {
            // Assumption: `value == null` and `error == null` are mutually exclusive.
            // i.e. When `error == null` then `CBCharacteristic`'s `value` is non-null.
            val data = didUpdateValueForCharacteristic.value!!
            DidUpdateValueForCharacteristic.Data(didUpdateValueForCharacteristic, data)
        } else {
            DidUpdateValueForCharacteristic.Error(didUpdateValueForCharacteristic, error)
        }

        _characteristicChanges.emitBlocking(change)
    }

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateValueForDescriptor: CBDescriptor,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "${peripheral.identifier} didUpdateValueForDescriptor"
            detail(didUpdateValueForDescriptor)
        }
        _response.sendBlocking(
            DidUpdateValueForDescriptor(peripheral.identifier, didUpdateValueForDescriptor, error),
        )
    }

    /* Writing Characteristic and Descriptor Values */

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun peripheral(
        peripheral: CBPeripheral,
        didWriteValueForCharacteristic: CBCharacteristic,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "${peripheral.identifier} didWriteValueForCharacteristic"
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

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun peripheral(
        peripheral: CBPeripheral,
        didWriteValueForDescriptor: CBDescriptor,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "${peripheral.identifier} didWriteValueForDescriptor"
            detail(didWriteValueForDescriptor)
        }
        // todo
    }

    override fun peripheralIsReadyToSendWriteWithoutResponse(
        peripheral: CBPeripheral,
    ) {
        logger.debug {
            message = "${peripheral.identifier} peripheralIsReadyToSendWriteWithoutResponse"
        }
        canSendWriteWithoutResponse.value = true
    }

    /* Managing Notifications for a Characteristic’s Value */

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateNotificationStateForCharacteristic: CBCharacteristic,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "${peripheral.identifier} didUpdateNotificationStateForCharacteristic"
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
            message = "${peripheral.identifier} didReadRSSI"
            detail("rssi", didReadRSSI.stringValue)
        }
        _response.sendBlocking(DidReadRssi(peripheral.identifier, didReadRSSI, error))
    }

    /* Monitoring Changes to a Peripheral’s Name or Services */

    override fun peripheralDidUpdateName(peripheral: CBPeripheral) {
        logger.debug {
            message = "${peripheral.identifier} peripheralDidUpdateName"
        }
        // todo
    }

    override fun peripheral(
        peripheral: CBPeripheral,
        didModifyServices: List<*>,
    ) {
        logger.debug {
            message = "${peripheral.identifier} didModifyServices"
        }
        // todo
    }

    /* Monitoring L2CAP Channels */

    override fun peripheral(
        peripheral: CBPeripheral,
        didOpenL2CAPChannel: CBL2CAPChannel?,
        error: NSError?,
    ) {
        logger.debug(error) {
            message = "${peripheral.identifier} didOpenL2CAPChannel"
        }
        // todo
    }

    fun close() {
        _response.close(ConnectionLostException())
        _characteristicChanges.emitBlocking(Closed)
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
