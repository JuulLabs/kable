package com.juul.kable

import com.juul.kable.PeripheralDelegate.Response.DidDiscoverServices
import com.juul.kable.PeripheralDelegate.Response.DidReadRssi
import com.juul.kable.PeripheralDelegate.Response.DidUpdateNotificationStateForCharacteristic
import com.juul.kable.PeripheralDelegate.Response.DidUpdateValueForDescriptor
import com.juul.kable.PeripheralDelegate.Response.DidWriteValueForCharacteristic
import com.juul.kable.PeripheralDelegate.Response.IsReadyToSendWriteWithoutResponse
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBPeripheralDelegateProtocol
import platform.CoreBluetooth.CBService
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.NSUUID
import platform.darwin.NSObject
import kotlin.native.concurrent.freeze

// https://developer.apple.com/documentation/corebluetooth/cbperipheraldelegate
internal class PeripheralDelegate : NSObject(), CBPeripheralDelegateProtocol {

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

        data class IsReadyToSendWriteWithoutResponse(
            override val peripheralIdentifier: NSUUID,
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

        abstract val cbCharacteristic: CBCharacteristic

        data class Data(
            override val cbCharacteristic: CBCharacteristic,
            val data: NSData,
        ) : DidUpdateValueForCharacteristic()

        data class Error(
            override val cbCharacteristic: CBCharacteristic,
            val error: NSError,
        ) : DidUpdateValueForCharacteristic()
    }

    // todo: MutableSharedFlow when Coroutines 1.4.x-mt is released.
    val _characteristicChanges = BroadcastChannel<DidUpdateValueForCharacteristic>(BUFFERED)
    val characteristicChanges: Flow<DidUpdateValueForCharacteristic> =
        _characteristicChanges.openSubscription().consumeAsFlow()

    /* Discovering Services */

    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverServices: NSError?,
    ) {
        println("<- PeripheralDelegate didDiscoverServices")
        _response.sendBlocking(DidDiscoverServices(peripheral.identifier, didDiscoverServices))
    }

    // todo: func peripheral(CBPeripheral, didDiscoverIncludedServicesFor: CBService, error: Error?)

    /* Discovering Characteristics and their Descriptors */

    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverCharacteristicsForService: CBService,
        error: NSError?
    ) {
        println("<- PeripheralDelegate didDiscoverCharacteristicsForService")
        _response.sendBlocking(
            Response.DidDiscoverCharacteristicsForService(
                peripheral.identifier,
                didDiscoverCharacteristicsForService,
                null
            )
        )
    }

    // todo: func peripheral(CBPeripheral, didDiscoverDescriptorsFor: CBCharacteristic, error: Error?)

    /* Retrieving Characteristic and Descriptor Values */

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateValueForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        println("<- PeripheralDelegate didUpdateValueForCharacteristic")
        val cbCharacteristic = didUpdateValueForCharacteristic.freeze()

        val change = if (error == null) {
            // Assumption: `value == null` and `error == null` are mutually exclusive.
            // i.e. When `error == null` then `CBCharacteristic`'s `value` is non-null.
            DidUpdateValueForCharacteristic.Data(cbCharacteristic, cbCharacteristic.value!!)
        } else {
            DidUpdateValueForCharacteristic.Error(cbCharacteristic, error)
        }

        _characteristicChanges.sendBlocking(change)
    }

    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateValueForDescriptor: CBDescriptor,
        error: NSError?
    ) {
        println("<- PeripheralDelegate didWriteValueForDescriptor")
        _response.sendBlocking(
            DidUpdateValueForDescriptor(peripheral.identifier, didUpdateValueForDescriptor, error)
        )
    }

    /* Writing Characteristic and Descriptor Values */

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun peripheral(
        peripheral: CBPeripheral,
        didWriteValueForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        println("<- PeripheralDelegate didWriteValueForCharacteristic")
        _response.sendBlocking(
            DidWriteValueForCharacteristic(
                peripheral.identifier,
                didWriteValueForCharacteristic,
                error
            )
        )
    }

    // todo: func peripheral(CBPeripheral, didWriteValueFor: CBDescriptor, error: Error?)

    override fun peripheralIsReadyToSendWriteWithoutResponse(
        peripheral: CBPeripheral
    ) {
        println("<- PeripheralDelegate isReadyToSendWriteWithoutResponse")
        _response.sendBlocking(
            IsReadyToSendWriteWithoutResponse(peripheral.identifier, error = null)
        )
    }

    /* Managing Notifications for a Characteristic’s Value */

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateNotificationStateForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        println("<- PeripheralDelegate didUpdateNotificationStateForCharacteristic")
        _response.sendBlocking(
            DidUpdateNotificationStateForCharacteristic(
                peripheral.identifier,
                didUpdateNotificationStateForCharacteristic,
                error
            )
        )
    }

    /* Retrieving a Peripheral’s RSSI Data */

    override fun peripheral(
        peripheral: CBPeripheral,
        didReadRSSI: NSNumber,
        error: NSError?,
    ) {
        println("<- PeripheralDelegate didReadRSSI")
        _response.sendBlocking(DidReadRssi(peripheral.identifier, didReadRSSI, error))
    }

    /* Monitoring Changes to a Peripheral’s Name or Services */

    // todo: func peripheralDidUpdateName(CBPeripheral)

    // todo: func peripheral(CBPeripheral, didModifyServices: [CBService])

    /* Monitoring L2CAP Channels */

    // todo: func peripheral(CBPeripheral, didOpen: CBL2CAPChannel?, error: Error?)

    fun close() {
        println("PeripheralDelegate close")
        _response.close(ConnectionLostException())
        _characteristicChanges.close()
    }
}
