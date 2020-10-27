package com.juul.kable.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTING
import android.util.Log
import com.juul.kable.ConnectionLostException
import com.juul.kable.TAG
import com.juul.kable.gatt.Response.OnCharacteristicRead
import com.juul.kable.gatt.Response.OnCharacteristicWrite
import com.juul.kable.gatt.Response.OnDescriptorRead
import com.juul.kable.gatt.Response.OnDescriptorWrite
import com.juul.kable.gatt.Response.OnMtuChanged
import com.juul.kable.gatt.Response.OnReadRemoteRssi
import com.juul.kable.gatt.Response.OnServicesDiscovered
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.runBlocking

private typealias DisconnectedAction = () -> Unit

internal class Callback : BluetoothGattCallback() {

    private var disconnectedAction: DisconnectedAction? = null
    fun invokeOnDisconnected(action: () -> Unit) {
        disconnectedAction = action
    }

    private val _onConnectionStateChange = MutableStateFlow<OnConnectionStateChange?>(null)
    val onConnectionStateChange: Flow<OnConnectionStateChange> =
        _onConnectionStateChange.filterNotNull()

    private val _onCharacteristicChanged = Channel<OnCharacteristicChanged>()
    val onCharacteristicChanged = _onCharacteristicChanged.consumeAsFlow()

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
        val event = OnConnectionStateChange(ConnectionStatus(status), ConnectionState(newState))
        _onConnectionStateChange.value = event

        if (newState == STATE_DISCONNECTING || newState == STATE_DISCONNECTED) {
            _onCharacteristicChanged.close(ConnectionLostException())
            onResponse.close(ConnectionLostException())

            if (newState == STATE_DISCONNECTED) {
                gatt.close()
                disconnectedAction?.invoke()
            }
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
        onResponse.offer(OnCharacteristicWrite(characteristic, GattStatus(status)))
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        val event = OnCharacteristicChanged(characteristic, characteristic.value)
        if (!_onCharacteristicChanged.offer(event)) {
            Log.w(TAG, "Subscribers are slow to consume, blocking thread for $event")
            runBlocking { _onCharacteristicChanged.send(event) }
        }
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
        onResponse.offer(OnDescriptorWrite(descriptor, GattStatus(status)))
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
