package com.juul.kable

import com.juul.kable.PeripheralDelegate.Response.DidDiscoverServices
import com.juul.kable.PeripheralDelegate.Response.DidReadRssi
import com.juul.kable.PeripheralDelegate.Response.DidUpdateNotificationStateForCharacteristic
import com.juul.kable.PeripheralDelegate.Response.DidWriteValueForCharacteristic
import com.juul.kable.PeripheralDelegate.Response.IsReadyToSendWriteWithoutResponse
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.id
import kotlinx.coroutines.runBlocking
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

    private val _characteristicChange = BroadcastChannel<DidUpdateValueForCharacteristic>(BUFFERED)
    val characteristicChange: Flow<DidUpdateValueForCharacteristic> = _characteristicChange.asFlow()

    override fun peripheral(peripheral: CBPeripheral, didDiscoverServices: NSError?) {
        println("PeripheralDelegate didDiscoverServices")
        _response.sendBlocking(DidDiscoverServices(peripheral.identifier, didDiscoverServices))
    }

    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverCharacteristicsForService: CBService,
        error: NSError?
    ) {
        println("PeripheralDelegate didDiscoverCharacteristicsForService")
        _response.sendBlocking(
            Response.DidDiscoverCharacteristicsForService(
                peripheral.identifier,
                didDiscoverCharacteristicsForService,
                null
            )
        )
    }
    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateNotificationStateForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        println("PeripheralDelegate didUpdateNotificationStateForCharacteristic")
        _response.sendBlocking(
            DidUpdateNotificationStateForCharacteristic(
                peripheral.identifier,
                didUpdateNotificationStateForCharacteristic,
                error
            )
        )
    }

    override fun peripheralIsReadyToSendWriteWithoutResponse(peripheral: CBPeripheral) {
        println("PeripheralDelegate IsReadyToSendWriteWithoutResponse")
        _response.sendBlocking(
            IsReadyToSendWriteWithoutResponse(
                peripheral.identifier,
                error = null
            )
        )
    }

    override fun peripheral(peripheral: CBPeripheral, didReadRSSI: NSNumber, error: NSError?) {
        println("PeripheralDelegate didReadRSSI")
        _response.sendBlocking(DidReadRssi(peripheral.identifier, didReadRSSI, error))
    }

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun peripheral(
        peripheral: CBPeripheral,
        didWriteValueForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        println("PeripheralDelegate didWriteValueForCharacteristic")
        _response.sendBlocking(
            DidWriteValueForCharacteristic(
                peripheral.identifier,
                didWriteValueForCharacteristic,
                error
            )
        )
    }

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateValueForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        println("PeripheralDelegate didUpdateValueForCharacteristic")
        val cbCharacteristic = didUpdateValueForCharacteristic.freeze()

        val change = if (error == null) {
            // Assumption: `value == null` and `error == null` are mutually exclusive.
            // i.e. When `error == null` then `CBCharacteristic`'s `value` is non-null.
            DidUpdateValueForCharacteristic.Data(cbCharacteristic, cbCharacteristic.value!!)
        } else {
            DidUpdateValueForCharacteristic.Error(cbCharacteristic, error)
        }

        _characteristicChange.sendBlocking(change)
    }
}

private fun <E> SendChannel<E>.sendBlocking(element: E) {
    if (offer(element)) return
    runBlocking { send(element) }
}
