package com.juul.kable.server

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile.STATE_CONNECTED
import android.bluetooth.BluetoothProfile.STATE_CONNECTING
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTING
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.MutableStateFlow

internal sealed class Request {
    data class OnServiceAdded(val status: Int)
}

internal sealed class State {
    data class Disconnected(val status: Int) : State()
    data object Connecting : State()
    data object Connected : State()
    data object Disconnecting : State()
}

internal class Callback : BluetoothGattServerCallback() {

    val state = MutableStateFlow<State>()
    val onServiceAdded = Channel<BluetoothGattService>(CONFLATED)

    override fun onConnectionStateChange(
        device: BluetoothDevice?,
        status: Int,
        newState: Int,
    ) {
        when (newState) {
            STATE_CONNECTING -> state.value = State.Connecting
            STATE_CONNECTED -> state.value = State.Connected
            STATE_DISCONNECTING -> state.value = State.Disconnecting
            STATE_DISCONNECTED -> state.value = State.Disconnected(status)
        }
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        if (status == GATT_SUCCESS && service != null) {
            onServiceAdded.trySendOrLog(service)
        } else {
            // todo: log error
        }
    }

    override fun onCharacteristicReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic?,
    ) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
    }

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        super.onCharacteristicWriteRequest(
            device,
            requestId,
            characteristic,
            preparedWrite,
            responseNeeded,
            offset,
            value,
        )
    }

    override fun onDescriptorReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        descriptor: BluetoothGattDescriptor?,
    ) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor)
    }

    override fun onDescriptorWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        descriptor: BluetoothGattDescriptor?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?,
    ) {
        super.onDescriptorWriteRequest(
            device,
            requestId,
            descriptor,
            preparedWrite,
            responseNeeded,
            offset,
            value,
        )
    }

    override fun onExecuteWrite(
        device: BluetoothDevice?,
        requestId: Int,
        execute: Boolean,
    ) {
        super.onExecuteWrite(device, requestId, execute)
    }

    override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
        // todo
    }

    override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
        // todo
    }

    override fun onPhyUpdate(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
        // todo
    }

    override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
        // todo
    }
}

private fun <E> SendChannel<E>.trySendOrLog(element: E) {
    trySend(element).onFailure { cause ->
        TODO() // log
    }
}
