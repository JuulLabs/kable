package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothDevice.BOND_BONDING
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.bluetooth.BluetoothDevice.ERROR
import android.bluetooth.BluetoothDevice.EXTRA_BOND_STATE
import android.bluetooth.BluetoothDevice.EXTRA_DEVICE
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.IntentCompat
import com.juul.kable.AndroidPeripheral.Bond
import com.juul.tuulbox.coroutines.flow.broadcastReceiverFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal fun bondStateFor(bluetoothDevice: BluetoothDevice): Flow<Bond> =
    broadcastReceiverFlow(IntentFilter(ACTION_BOND_STATE_CHANGED))
        .onEach { intent ->
            println("Bond state for ${intent.bluetoothDevice}: ${intent.bondState}")
        }
        .filter { intent -> bluetoothDevice == intent.bluetoothDevice }
        .map { intent -> intent.bondState }
        .map(::Bond)

internal fun Bond(state: Int): Bond = when (state) {
    BOND_NONE -> Bond.None
    BOND_BONDING -> Bond.Bonding
    BOND_BONDED -> Bond.Bonded
    else -> error("Unsupported bond state: $state")
}

private val Intent.bluetoothDevice: BluetoothDevice?
    get() = IntentCompat.getParcelableExtra(this, EXTRA_DEVICE, BluetoothDevice::class.java)

private val Intent.bondState: Int
    get() = getIntExtra(EXTRA_BOND_STATE, ERROR)
