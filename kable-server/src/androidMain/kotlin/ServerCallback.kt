package com.juul.kable.server

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile.STATE_CONNECTED
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import com.juul.kable.server.logs.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.onFailure
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.toKotlinUuid

/**
 * Translates [BluetoothGattServerCallback] callbacks (which are invoked on binder threads) into
 * [InboundRequest]s (processed by the [RequestDispatcher]).
 */
internal class ServerCallback(
    private val logger: Logger,
) : BluetoothGattServerCallback() {

    /** Set (immediately after `openGattServer` returns) before any callback can fire. */
    @Volatile
    lateinit var server: BluetoothGattServer

    private val _requests = Channel<InboundRequest>(UNLIMITED)
    val requests: ReceiveChannel<InboundRequest> = _requests

    /** [GATT_SUCCESS] (or failure status) of the most recent `addService` call. */
    val onServiceAdded = Channel<Int>(UNLIMITED)

    /** [GATT_SUCCESS] (or failure status) of the most recent `notifyCharacteristicChanged` call. */
    val onNotificationSent = Channel<Int>(CONFLATED)

    private val mtus = ConcurrentHashMap<String, Int>()
    private val centrals = ConcurrentHashMap<String, AndroidCentral>()

    fun close() {
        _requests.close()
    }

    private fun central(device: BluetoothDevice): AndroidCentral =
        centrals.getOrPut(device.address) {
            AndroidCentral(device) { mtus[device.address] ?: DEFAULT_ATT_MTU }
        }

    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        logger.debug { "onConnectionStateChange address=${device.address} status=$status newState=$newState" }
        when (newState) {
            STATE_CONNECTED -> _requests.trySendOrLog(InboundRequest.CentralConnected(central(device)))
            STATE_DISCONNECTED -> {
                val central = central(device)
                mtus.remove(device.address)
                centrals.remove(device.address)
                _requests.trySendOrLog(InboundRequest.CentralDisconnected(central))
            }
        }
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        logger.debug { "onServiceAdded uuid=${service?.uuid} status=$status" }
        onServiceAdded.trySendOrLog(status)
    }

    override fun onCharacteristicReadRequest(
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic,
    ) {
        logger.debug { "onCharacteristicReadRequest address=${device.address} uuid=${characteristic.uuid} offset=$offset" }
        _requests.trySendOrLog(
            InboundRequest.Read(
                central = central(device),
                attribute = characteristic.attributeKey,
                offset = offset,
                // `respond` is invoked with the value already sliced at the requested offset (by
                // the `RequestDispatcher`), matching the `sendResponse` contract: the stack does
                // not slice `value` (the `offset` argument only identifies the partial-read
                // response), so the payload passed to `sendResponse` must be the portion of the
                // attribute value starting at `offset`.
                respond = { value -> sendResponse(device, requestId, GATT_SUCCESS, offset, value) },
                fail = { error -> sendResponse(device, requestId, error.code, offset, null) },
            ),
        )
    }

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?,
    ) {
        logger.debug {
            "onCharacteristicWriteRequest address=${device.address} uuid=${characteristic.uuid} " +
                "preparedWrite=$preparedWrite responseNeeded=$responseNeeded offset=$offset" +
                logger.data(value ?: ByteArray(0))
        }
        onWriteRequest(device, requestId, characteristic.attributeKey, preparedWrite, responseNeeded, offset, value)
    }

    override fun onDescriptorReadRequest(
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        descriptor: BluetoothGattDescriptor,
    ) {
        logger.debug { "onDescriptorReadRequest address=${device.address} uuid=${descriptor.uuid} offset=$offset" }
        _requests.trySendOrLog(
            InboundRequest.Read(
                central = central(device),
                attribute = descriptor.attributeKey,
                offset = offset,
                // See `onCharacteristicReadRequest` (above) regarding offset/slicing handling.
                respond = { value -> sendResponse(device, requestId, GATT_SUCCESS, offset, value) },
                fail = { error -> sendResponse(device, requestId, error.code, offset, null) },
            ),
        )
    }

    override fun onDescriptorWriteRequest(
        device: BluetoothDevice,
        requestId: Int,
        descriptor: BluetoothGattDescriptor,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?,
    ) {
        logger.debug {
            "onDescriptorWriteRequest address=${device.address} uuid=${descriptor.uuid} " +
                "preparedWrite=$preparedWrite responseNeeded=$responseNeeded offset=$offset" +
                logger.data(value ?: ByteArray(0))
        }
        onWriteRequest(device, requestId, descriptor.attributeKey, preparedWrite, responseNeeded, offset, value)
    }

    private fun onWriteRequest(
        device: BluetoothDevice,
        requestId: Int,
        attribute: AttributeKey,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?,
    ) {
        val bytes = value?.copyOf() ?: ByteArray(0)
        _requests.trySendOrLog(
            InboundRequest.Write(
                central = central(device),
                attribute = attribute,
                value = bytes,
                offset = offset,
                prepared = preparedWrite,
                respond = if (responseNeeded) {
                    { sendResponse(device, requestId, GATT_SUCCESS, offset, bytes) }
                } else {
                    null
                },
                fail = if (responseNeeded) {
                    { error -> sendResponse(device, requestId, error.code, offset, null) }
                } else {
                    null
                },
            ),
        )
    }

    override fun onExecuteWrite(device: BluetoothDevice, requestId: Int, execute: Boolean) {
        logger.debug { "onExecuteWrite address=${device.address} execute=$execute" }
        _requests.trySendOrLog(
            InboundRequest.ExecuteWrite(
                central = central(device),
                commit = execute,
                respond = { sendResponse(device, requestId, GATT_SUCCESS, 0, null) },
                fail = { error -> sendResponse(device, requestId, error.code, 0, null) },
            ),
        )
    }

    override fun onNotificationSent(device: BluetoothDevice, status: Int) {
        logger.debug { "onNotificationSent address=${device.address} status=$status" }
        onNotificationSent.trySendOrLog(status)
    }

    override fun onMtuChanged(device: BluetoothDevice, mtu: Int) {
        logger.debug { "onMtuChanged address=${device.address} mtu=$mtu" }
        mtus[device.address] = mtu
    }

    private fun sendResponse(device: BluetoothDevice, requestId: Int, status: Int, offset: Int, value: ByteArray?) {
        if (!server.sendResponse(device, requestId, status, offset, value)) {
            logger.warn { "sendResponse failed for address=${device.address} requestId=$requestId status=$status" }
        }
    }

    private fun <E> SendChannel<E>.trySendOrLog(element: E) {
        trySend(element).onFailure { cause ->
            logger.warn(cause) { "Unable to deliver $element" }
        }
    }
}

private val BluetoothGattCharacteristic.attributeKey: AttributeKey.Characteristic
    get() = AttributeKey.Characteristic(
        serviceUuid = service.uuid.toKotlinUuid(),
        characteristicUuid = uuid.toKotlinUuid(),
    )

private val BluetoothGattDescriptor.attributeKey: AttributeKey.Descriptor
    get() = AttributeKey.Descriptor(
        serviceUuid = characteristic.service.uuid.toKotlinUuid(),
        characteristicUuid = characteristic.uuid.toKotlinUuid(),
        descriptorUuid = uuid.toKotlinUuid(),
    )
