package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothDevice.BOND_BONDING
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.bluetooth.BluetoothDevice.ERROR
import android.bluetooth.BluetoothDevice.EXTRA_BOND_STATE
import android.bluetooth.BluetoothDevice.EXTRA_DEVICE
import android.bluetooth.BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_EXPORTED
import androidx.core.content.IntentCompat
import com.juul.kable.AndroidPeripheral.Bond
import com.juul.tuulbox.coroutines.flow.broadcastReceiverFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlin.time.Duration.Companion.milliseconds

private val bondStateChanged = IntentFilter(ACTION_BOND_STATE_CHANGED)

// The Bluetooth app sends this broadcast under its own uid, which a not-exported receiver refuses on every API level.
// It is a protected broadcast, so exporting admits no other sender.
private const val BOND_RECEIVER_FLAGS = RECEIVER_EXPORTED

private val BOND_POLL_INTERVAL = 500.milliseconds

// `createBond()` returning `true` only means the request was queued; if the stack then fails it, the state stays
// `BOND_NONE` and no broadcast is sent. This many `BOND_NONE` polls without seeing `BOND_BONDING` count as failure.
private const val STARTED_BOND_NONE_POLLS = 3

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
 * [Bond.None]. The receiver is registered before `createBond()` is called, so no transition can be missed, and
 * `bondState` is polled alongside it, since a bond the stack fails after `createBond()` sends no broadcast.
 */
internal suspend fun BluetoothDevice.createBondAndAwait(): Bond = channelFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.bluetoothDevice != this@createBondAndAwait) return
            when (intent.bondState) {
                BOND_BONDED -> trySend(Bond.Bonded)
                BOND_BONDING -> trySend(Bond.Bonding)
                // A `BOND_NONE` from an earlier removal is not this bond's outcome.
                BOND_NONE -> if (intent.previousBondState == BOND_BONDING) trySend(Bond.None)
            }
        }
    }
    ContextCompat.registerReceiver(applicationContext, receiver, bondStateChanged, BOND_RECEIVER_FLAGS)

    try {
        val outcome = when (bondState) {
            BOND_BONDED -> Bond.Bonded
            BOND_BONDING -> pollBondOutcome(bondingSeen = true)
            else -> when {
                createBond() -> pollBondOutcome(bondingSeen = false)
                bondState == BOND_BONDING -> pollBondOutcome(bondingSeen = true)
                else -> Bond(bondState) ?: Bond.None
            }
        }
        send(outcome)
    } finally {
        applicationContext.unregisterReceiver(receiver)
    }
}.first { it != Bond.Bonding }

private suspend fun BluetoothDevice.pollBondOutcome(bondingSeen: Boolean): Bond {
    var bonding = bondingSeen
    var nonePolls = 0
    while (true) {
        delay(BOND_POLL_INTERVAL)
        when (bondState) {
            BOND_BONDED -> return Bond.Bonded
            BOND_BONDING -> bonding = true
            BOND_NONE -> if (bonding || ++nonePolls >= STARTED_BOND_NONE_POLLS) return Bond.None
        }
    }
}

private val Intent.bluetoothDevice: BluetoothDevice?
    get() = IntentCompat.getParcelableExtra(this, EXTRA_DEVICE, BluetoothDevice::class.java)

private val Intent.bondState: Int
    get() = getIntExtra(EXTRA_BOND_STATE, ERROR)

private val Intent.previousBondState: Int
    get() = getIntExtra(EXTRA_PREVIOUS_BOND_STATE, ERROR)
