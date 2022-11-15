package com.juul.kable

import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.bluetooth.BluetoothAdapter.ERROR
import android.bluetooth.BluetoothAdapter.EXTRA_STATE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

private val intentFilter = IntentFilter(ACTION_STATE_CHANGED)

@OptIn(DelicateCoroutinesApi::class)
internal val bluetoothState = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(EXTRA_STATE, ERROR)
            channel.trySend(state)
        }
    }
    applicationContext.registerReceiver(receiver, intentFilter)
    awaitClose {
        applicationContext.unregisterReceiver(receiver)
    }
}.shareIn(GlobalScope, started = WhileSubscribed(replayExpirationMillis = 0))
