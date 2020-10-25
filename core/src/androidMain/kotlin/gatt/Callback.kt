package com.juul.kable.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTING
import android.util.Log
import com.juul.kable.TAG
import com.juul.kable.gatt.Response.OnCharacteristicRead
import com.juul.kable.gatt.Response.OnCharacteristicWrite
import com.juul.kable.gatt.Response.OnDescriptorRead
import com.juul.kable.gatt.Response.OnDescriptorWrite
import com.juul.kable.gatt.Response.OnMtuChanged
import com.juul.kable.gatt.Response.OnReadRemoteRssi
import com.juul.kable.gatt.Response.OnServicesDiscovered
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

public class GattStatusException internal constructor(
    message: String?
) : IOException(message) {
    internal constructor(
        status: GattStatus,
        prefix: String
    ) : this("$prefix failed with status $status")
}

public class ConnectionLostException internal constructor(
    message: String? = null,
    cause: Throwable? = null
) : IOException(message, cause)

private val Success = ConnectionStatus(GATT_SUCCESS)
private val Disconnected = ConnectionState(STATE_DISCONNECTED)

internal class Callback(
    private val dispatcher: ExecutorCoroutineDispatcher
) : BluetoothGattCallback() {

    private val _onConnectionStateChange = MutableStateFlow<OnConnectionStateChange?>(null)
    val onConnectionStateChange: Flow<OnConnectionStateChange> =
        _onConnectionStateChange.filterNotNull()

    private val _onCharacteristicChanged = Channel<OnCharacteristicChanged>()
    val onCharacteristicChanged = _onCharacteristicChanged.consumeAsFlow()
    val onResponse = Channel<Response>(CONFLATED)

    private val isClosed = AtomicBoolean()

    private fun onDisconnecting() {
        _onCharacteristicChanged.close(ConnectionLostException())
        onResponse.close(ConnectionLostException())
    }

    fun close(gatt: BluetoothGatt) {
        if (isClosed.compareAndSet(false, true)) {
            Log.v(TAG, "Closing GattCallback belonging to device ${gatt.device}")
            onDisconnecting() // Duplicate call in case Android skips STATE_DISCONNECTING.
            gatt.close()

            _onConnectionStateChange.value = OnConnectionStateChange(Success, Disconnected)

            // todo: Remove when https://github.com/Kotlin/kotlinx.coroutines/issues/261 is fixed.
            dispatcher.close()
        }
    }

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

        when (newState) {
            STATE_DISCONNECTING -> onDisconnecting()
            STATE_DISCONNECTED -> close(gatt)
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
