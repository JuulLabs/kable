package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothDevice.BOND_BONDING
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.bluetooth.BluetoothDevice.ERROR
import android.bluetooth.BluetoothDevice.EXTRA_BOND_STATE
import android.bluetooth.BluetoothDevice.EXTRA_DEVICE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_EXPORTED
import androidx.core.content.IntentCompat
import com.juul.kable.AndroidPeripheral.Bond
import com.juul.tuulbox.coroutines.flow.broadcastReceiverFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

private val bondStateChanged = IntentFilter(ACTION_BOND_STATE_CHANGED)

// Exported: the Bluetooth process sends this protected broadcast, and below API 33 `RECEIVER_NOT_EXPORTED` guards the
// receiver with an app-signature permission that sender does not hold.
private const val BOND_RECEIVER_FLAGS = RECEIVER_EXPORTED

internal fun BluetoothDevice.bondStates(): Flow<Bond> =
    broadcastReceiverFlow(bondStateChanged, BOND_RECEIVER_FLAGS)
        .filter { intent -> intent.bluetoothDevice == this }
        .mapNotNull { intent -> Bond(intent.bondState) }

internal fun Bond(state: Int): Bond? = when (state) {
    BOND_NONE -> Bond.None
    BOND_BONDING -> Bond.Bonding
    BOND_BONDED -> Bond.Bonded
    else -> null
}

/**
 * Starts bonding (unless already bonded or bonding) and suspends until it settles, returning [Bond.Bonded] or
 * [Bond.None]. The receiver is registered before `createBond()` is called, so no transition can be missed.
 */
internal suspend fun BluetoothDevice.createBondAndAwait(): Bond = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.bluetoothDevice == this@createBondAndAwait) {
                Bond(intent.bondState)?.let(::trySend)
            }
        }
    }
    ContextCompat.registerReceiver(applicationContext, receiver, bondStateChanged, BOND_RECEIVER_FLAGS)

    when (bondState) {
        BOND_BONDED -> trySend(Bond.Bonded)
        BOND_BONDING -> Unit // Already in progress; await its outcome.
        else -> if (!createBond() && bondState != BOND_BONDING) trySend(Bond(bondState) ?: Bond.None)
    }

    awaitClose { applicationContext.unregisterReceiver(receiver) }
}.first { it != Bond.Bonding }

private val Intent.bluetoothDevice: BluetoothDevice?
    get() = IntentCompat.getParcelableExtra(this, EXTRA_DEVICE, BluetoothDevice::class.java)

private val Intent.bondState: Int
    get() = getIntExtra(EXTRA_BOND_STATE, ERROR)
