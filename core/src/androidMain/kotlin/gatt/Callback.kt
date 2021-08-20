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
import com.juul.kable.logs.detail
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow

private typealias DisconnectedAction = () -> Unit

internal data class OnCharacteristicChanged(
    val characteristic: BluetoothGattCharacteristic,
    val value: ByteArray,
)

internal class Callback(
    private val state: MutableStateFlow<State>,
    private val mtu: MutableStateFlow<Int?>,
    logging: Logging,
    macAddress: String,
) : BluetoothGattCallback() {

    private val logger = Logger(logging, tag = "Kable/Callback", prefix = "$macAddress ")

    private var disconnectedAction: DisconnectedAction? = null
    fun invokeOnDisconnected(action: DisconnectedAction) {
        disconnectedAction = action
    }

    private val _onCharacteristicChanged = Channel<OnCharacteristicChanged>(UNLIMITED)
    val onCharacteristicChanged: Flow<OnCharacteristicChanged> =
        _onCharacteristicChanged.consumeAsFlow()

    val onResponse = Channel<Response>(CONFLATED)
    val onMtuChanged = Channel<OnMtuChanged>(CONFLATED)

    override fun onPhyUpdate(
        gatt: BluetoothGatt,
        txPhy: Int,
        rxPhy: Int,
        status: Int
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
        status: Int
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
        newState: Int
    ) {
        logger.debug {
            message = "onConnectionStateChange"
            detail("status", status.disconnectedConnectionStatusString)
            detail("newState", newState.connectionStateString)
        }

        if (newState == STATE_DISCONNECTED) {
            gatt.close()
            disconnectedAction?.invoke()
        }

        when (newState) {
            STATE_CONNECTING -> state.value = State.Connecting
            STATE_CONNECTED -> state.value = State.Connected
            STATE_DISCONNECTING -> state.value = State.Disconnecting
            STATE_DISCONNECTED -> state.value = State.Disconnected(status.disconnectedConnectionStatus)
        }

        if (newState == STATE_DISCONNECTING || newState == STATE_DISCONNECTED) {
            _onCharacteristicChanged.close()
            onResponse.close(ConnectionLostException())
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        val event = OnServicesDiscovered(GattStatus(status))
        logger.debug {
            message = "onServicesDiscovered"
            detail(event.status)
        }
        logger.trySendOrLog(onResponse, event)
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int,
    ) {
        val value = characteristic.value
        val event = OnCharacteristicRead(characteristic, value, GattStatus(status))
        logger.debug {
            message = "onCharacteristicRead"
            detail(characteristic)
            detail(event.status)
            detail(value)
        }
        logger.trySendOrLog(onResponse, event)
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
        logger.trySendOrLog(onResponse, event)
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        val value = characteristic.value
        val event = OnCharacteristicChanged(characteristic, value)
        logger.debug {
            message = "onCharacteristicChanged"
            detail(characteristic)
            detail(value)
        }
        logger.trySendOrLog(_onCharacteristicChanged, event)
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int,
    ) {
        val value = descriptor.value
        val event = OnDescriptorRead(descriptor, value, GattStatus(status))
        logger.debug {
            message = "onDescriptorRead"
            detail(descriptor)
            detail(event.status)
            detail(value)
        }
        logger.trySendOrLog(onResponse, event)
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
        logger.trySendOrLog(onResponse, event)
    }

    override fun onReliableWriteCompleted(
        gatt: BluetoothGatt,
        status: Int
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
        logger.trySendOrLog(onResponse, event)
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
        logger.trySendOrLog(onMtuChanged, event)
        if (status == GATT_SUCCESS) this.mtu.value = mtu
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

private fun <E> Logger.trySendOrLog(channel: SendChannel<E>, element: E) {
    channel.trySend(element).getOrElse { cause ->
        warn(cause) {
            message = "Callback was unable to deliver $element"
        }
    }
}
