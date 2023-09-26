@file:Suppress("DeprecatedCallableAddReplaceWith") // `ReplaceWith` is unnecessary for `internal` class.

package com.juul.kable.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile.STATE_CONNECTED
import android.bluetooth.BluetoothProfile.STATE_CONNECTING
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTING
import com.juul.kable.ConnectionLostException
import com.juul.kable.ObservationEvent
import com.juul.kable.ObservationEvent.CharacteristicChange
import com.juul.kable.State
import com.juul.kable.State.Disconnected.Status.Cancelled
import com.juul.kable.State.Disconnected.Status.CentralDisconnected
import com.juul.kable.State.Disconnected.Status.Failed
import com.juul.kable.State.Disconnected.Status.L2CapFailure
import com.juul.kable.State.Disconnected.Status.LinkManagerProtocolTimeout
import com.juul.kable.State.Disconnected.Status.PeripheralDisconnected
import com.juul.kable.State.Disconnected.Status.Timeout
import com.juul.kable.State.Disconnected.Status.Unknown
import com.juul.kable.external.GATT_CONN_CANCEL
import com.juul.kable.external.GATT_CONN_FAIL_ESTABLISH
import com.juul.kable.external.GATT_CONN_L2C_FAILURE
import com.juul.kable.external.GATT_CONN_LMP_TIMEOUT
import com.juul.kable.external.GATT_CONN_TERMINATE_LOCAL_HOST
import com.juul.kable.external.GATT_CONN_TERMINATE_PEER_USER
import com.juul.kable.external.GATT_CONN_TIMEOUT
import com.juul.kable.gatt.Response.OnCharacteristicRead
import com.juul.kable.gatt.Response.OnCharacteristicWrite
import com.juul.kable.gatt.Response.OnDescriptorRead
import com.juul.kable.gatt.Response.OnDescriptorWrite
import com.juul.kable.gatt.Response.OnReadRemoteRssi
import com.juul.kable.gatt.Response.OnServicesDiscovered
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.logs.Logging.DataProcessor.Operation.Change
import com.juul.kable.logs.Logging.DataProcessor.Operation.Read
import com.juul.kable.logs.detail
import com.juul.kable.toLazyCharacteristic
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

internal class Callback(
    private val state: MutableStateFlow<State>,
    private val mtu: MutableStateFlow<Int?>,
    private val onCharacteristicChanged: MutableSharedFlow<ObservationEvent<ByteArray>>,
    logging: Logging,
    macAddress: String,
) : BluetoothGattCallback() {

    private val logger = Logger(logging, tag = "Kable/Callback", identifier = macAddress)

    val onResponse = Channel<Response>(CONFLATED)
    val onMtuChanged = Channel<OnMtuChanged>(CONFLATED)

    override fun onPhyUpdate(
        gatt: BluetoothGatt,
        txPhy: Int,
        rxPhy: Int,
        status: Int,
    ) {
        logger.debug {
            message = "onPhyUpdate"
            detail("txPhy", txPhy)
            detail("rxPhy", rxPhy)
            detail("status", status)
        }
        // todo
    }

    override fun onPhyRead(
        gatt: BluetoothGatt,
        txPhy: Int,
        rxPhy: Int,
        status: Int,
    ) {
        logger.debug {
            message = "onPhyRead"
            detail("txPhy", txPhy)
            detail("rxPhy", rxPhy)
            detail("status", status)
        }
        // todo
    }

    override fun onConnectionStateChange(
        gatt: BluetoothGatt,
        status: Int,
        newState: Int,
    ) {
        logger.debug {
            message = "onConnectionStateChange"
            detail("status", status.disconnectedConnectionStatusString)
            detail("newState", newState.connectionStateString)
        }

        if (newState == STATE_DISCONNECTED) gatt.close()

        when (newState) {
            STATE_CONNECTING -> state.value = State.Connecting.Bluetooth
            STATE_CONNECTED -> state.value = State.Connecting.Services
            STATE_DISCONNECTING -> state.value = State.Disconnecting
            STATE_DISCONNECTED -> state.value = State.Disconnected(status.disconnectedConnectionStatus)
        }

        if (newState == STATE_DISCONNECTING || newState == STATE_DISCONNECTED) {
            onResponse.close(ConnectionLostException())
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        val event = OnServicesDiscovered(GattStatus(status))
        logger.debug {
            message = "onServicesDiscovered"
            detail(event.status)
        }
        onResponse.trySendOrLog(event)
    }

    @Deprecated("Deprecated in API 33.")
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int,
    ) {
        @Suppress("DEPRECATION")
        onCharacteristicRead(gatt, characteristic, characteristic.value ?: byteArrayOf(), status)
    }

    // Added in API 33.
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int,
    ) {
        val event = OnCharacteristicRead(characteristic, value, GattStatus(status))
        logger.debug {
            message = "onCharacteristicRead"
            detail(characteristic)
            detail(event.status)
            detail(value, Read)
        }
        onResponse.trySendOrLog(event)
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int,
    ) {
        val event = OnCharacteristicWrite(characteristic, GattStatus(status))
        logger.debug {
            message = "onCharacteristicWrite"
            detail(characteristic)
            detail(event.status)
        }
        onResponse.trySendOrLog(event)
    }

    @Deprecated("Deprecated in API 33.")
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
    ) {
        @Suppress("DEPRECATION")
        onCharacteristicChanged(gatt, characteristic, characteristic.value ?: byteArrayOf())
    }

    // Added in API 33.
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
    ) {
        logger.debug {
            message = "onCharacteristicChanged"
            detail(characteristic)
            detail(value, Change)
        }
        val event = CharacteristicChange(characteristic.toLazyCharacteristic(), value)
        onCharacteristicChanged.tryEmitOrLog(event)
    }

    @Deprecated("Deprecated in API 33.")
    override fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int,
    ) {
        @Suppress("DEPRECATION")
        onDescriptorRead(gatt, descriptor, status, descriptor.value ?: byteArrayOf())
    }

    // Added in API 33.
    override fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int,
        value: ByteArray,
    ) {
        val event = OnDescriptorRead(descriptor, value, GattStatus(status))
        logger.debug {
            message = "onDescriptorRead"
            detail(descriptor)
            detail(event.status)
            detail(value, Read)
        }
        onResponse.trySendOrLog(event)
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int,
    ) {
        val event = OnDescriptorWrite(descriptor, GattStatus(status))
        logger.debug {
            message = "onDescriptorWrite"
            detail(descriptor)
            detail(event.status)
        }
        onResponse.trySendOrLog(event)
    }

    override fun onReliableWriteCompleted(
        gatt: BluetoothGatt,
        status: Int,
    ) {
        logger.debug {
            message = "onReliableWriteCompleted"
            detail(GattStatus(status))
        }
        // todo
    }

    override fun onReadRemoteRssi(
        gatt: BluetoothGatt,
        rssi: Int,
        status: Int,
    ) {
        val event = OnReadRemoteRssi(rssi, GattStatus(status))
        logger.debug {
            message = "onReadRemoteRssi"
            detail("rssi", event.rssi)
            detail(event.status)
        }
        onResponse.trySendOrLog(event)
    }

    override fun onMtuChanged(
        gatt: BluetoothGatt,
        mtu: Int,
        status: Int,
    ) {
        val event = OnMtuChanged(mtu, GattStatus(status))
        logger.debug {
            message = "onMtuChanged"
            detail("mtu", event.mtu)
            detail(event.status)
        }
        onMtuChanged.trySendOrLog(event)
        if (status == GATT_SUCCESS) this.mtu.value = mtu
    }

    private fun <E> SendChannel<E>.trySendOrLog(element: E) {
        trySend(element).onFailure { cause ->
            logger.warn(cause) {
                message = "Callback was unable to deliver $element"
            }
        }
    }

    private fun <E> MutableSharedFlow<E>.tryEmitOrLog(element: E) {
        if (!tryEmit(element)) {
            logger.warn {
                message = "Callback was unable to deliver $element"
            }
        }
    }
}

private val Int.disconnectedConnectionStatus: State.Disconnected.Status?
    get() = when (this) {
        GATT_SUCCESS -> null
        GATT_CONN_L2C_FAILURE -> L2CapFailure
        GATT_CONN_TIMEOUT -> Timeout
        GATT_CONN_TERMINATE_PEER_USER -> PeripheralDisconnected
        GATT_CONN_TERMINATE_LOCAL_HOST -> CentralDisconnected
        GATT_CONN_FAIL_ESTABLISH -> Failed
        GATT_CONN_LMP_TIMEOUT -> LinkManagerProtocolTimeout
        GATT_CONN_CANCEL -> Cancelled
        else -> Unknown(this)
    }

private val Int.disconnectedConnectionStatusString: String
    get() = when (val status = disconnectedConnectionStatus) {
        null -> "Success"
        is Unknown -> "Unknown($status)"
        else -> toString()
    }

private val Int.connectionStateString: String
    get() = when (this) {
        STATE_CONNECTING -> "Connecting"
        STATE_CONNECTED -> "Connected"
        STATE_DISCONNECTING -> "Disconnecting"
        STATE_DISCONNECTED -> "Disconnected"
        else -> "Unknown($this)"
    }
