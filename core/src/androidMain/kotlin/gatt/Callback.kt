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
import android.util.Log
import com.juul.kable.ConnectionLostException
import com.juul.kable.State
import com.juul.kable.State.Disconnected.Status.Cancelled
import com.juul.kable.State.Disconnected.Status.Failed
import com.juul.kable.State.Disconnected.Status.PeripheralDisconnected
import com.juul.kable.State.Disconnected.Status.Timeout
import com.juul.kable.State.Disconnected.Status.Unknown
import com.juul.kable.TAG
import com.juul.kable.external.GATT_CONN_CANCEL
import com.juul.kable.external.GATT_CONN_FAIL_ESTABLISH
import com.juul.kable.external.GATT_CONN_TERMINATE_PEER_USER
import com.juul.kable.external.GATT_CONN_TIMEOUT
import com.juul.kable.gatt.Response.OnCharacteristicRead
import com.juul.kable.gatt.Response.OnCharacteristicWrite
import com.juul.kable.gatt.Response.OnDescriptorRead
import com.juul.kable.gatt.Response.OnDescriptorWrite
import com.juul.kable.gatt.Response.OnMtuChanged
import com.juul.kable.gatt.Response.OnReadRemoteRssi
import com.juul.kable.gatt.Response.OnServicesDiscovered
import com.juul.kable.toHexString
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow

private typealias DisconnectedAction = () -> Unit

internal data class OnCharacteristicChanged(
    val characteristic: BluetoothGattCharacteristic,
    val value: ByteArray,
) {
    override fun toString(): String = "OnCharacteristicChanged(characteristic=${characteristic.uuid}, value=${value.toHexString()})"
}

internal class Callback(
    private val state: MutableStateFlow<State>
) : BluetoothGattCallback() {

    private var disconnectedAction: DisconnectedAction? = null
    fun invokeOnDisconnected(action: DisconnectedAction) {
        disconnectedAction = action
    }

    private val _onCharacteristicChanged = Channel<OnCharacteristicChanged>(UNLIMITED)
    val onCharacteristicChanged: Flow<OnCharacteristicChanged> =
        _onCharacteristicChanged.consumeAsFlow()

    val onResponse = Channel<Response>(CONFLATED)

    override fun onPhyUpdate(
        gatt: BluetoothGatt,
        txPhy: Int,
        rxPhy: Int,
        status: Int
    ) {
        // todo
    }

    override fun onPhyRead(
        gatt: BluetoothGatt,
        txPhy: Int,
        rxPhy: Int,
        status: Int
    ) {
        // todo
    }

    override fun onConnectionStateChange(
        gatt: BluetoothGatt,
        status: Int,
        newState: Int
    ) {
        if (newState == STATE_DISCONNECTED) {
            gatt.close()
            disconnectedAction?.invoke()
        }

        when (newState) {
            STATE_CONNECTING -> state.value = State.Connecting
            STATE_CONNECTED -> state.value = State.Connected
            STATE_DISCONNECTING -> state.value = State.Disconnecting
            STATE_DISCONNECTED -> state.value = State.Disconnected(status.toStatus())
        }

        if (newState == STATE_DISCONNECTING || newState == STATE_DISCONNECTED) {
            _onCharacteristicChanged.close()
            onResponse.close(ConnectionLostException())
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        onResponse.offer(OnServicesDiscovered(GattStatus(status)))
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int,
    ) {
        val value = characteristic.value
        onResponse.offer(OnCharacteristicRead(characteristic, value, GattStatus(status)))
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int,
    ) {
        val event = OnCharacteristicWrite(characteristic, GattStatus(status))
        Log.d(TAG, "OnCharacteristicWrite(characteristic=${event.characteristic.uuid}, status=${event.status})")
        onResponse.offer(event)
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        val event = OnCharacteristicChanged(characteristic, characteristic.value)
        Log.d(TAG, "Received $event")
        _onCharacteristicChanged.offer(event)
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int,
    ) {
        onResponse.offer(OnDescriptorRead(descriptor, descriptor.value, GattStatus(status)))
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int,
    ) {
        val event = OnDescriptorWrite(descriptor, GattStatus(status))
        Log.d(TAG, "OnDescriptorWrite(descriptor=${event.descriptor.uuid}, status=${event.status})")
        onResponse.offer(event)
    }

    override fun onReliableWriteCompleted(
        gatt: BluetoothGatt,
        status: Int
    ) {
        // todo
    }

    override fun onReadRemoteRssi(
        gatt: BluetoothGatt,
        rssi: Int,
        status: Int,
    ) {
        onResponse.offer(OnReadRemoteRssi(rssi, GattStatus(status)))
    }

    override fun onMtuChanged(
        gatt: BluetoothGatt,
        mtu: Int,
        status: Int,
    ) {
        onResponse.offer(OnMtuChanged(mtu, GattStatus(status)))
    }
}

private fun Int.toStatus(): State.Disconnected.Status? = when (this) {
    GATT_SUCCESS -> null
    GATT_CONN_TIMEOUT -> Timeout
    GATT_CONN_TERMINATE_PEER_USER -> PeripheralDisconnected
    GATT_CONN_FAIL_ESTABLISH -> Failed
    GATT_CONN_CANCEL -> Cancelled
    else -> Unknown(this)
}
