package com.juul.kable

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

private val bluetoothStateIntentFilter: IntentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

private inline fun bluetoothStateBroadcastReceiver(
    crossinline action: (state: Int) -> Unit,
): BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
        action.invoke(state)
    }
}

internal inline fun registerBluetoothStateBroadcastReceiver(
    context: Context = applicationContext,
    crossinline action: (state: Int) -> Unit,
): BroadcastReceiver = bluetoothStateBroadcastReceiver(action)
    .also { context.registerReceiver(it, bluetoothStateIntentFilter) }
