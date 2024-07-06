package com.juul.kable

import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.bluetooth.BluetoothAdapter.ERROR
import android.bluetooth.BluetoothAdapter.EXTRA_STATE
import android.content.IntentFilter
import com.juul.tuulbox.coroutines.flow.broadcastReceiverFlow
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

private val intentFilter = IntentFilter(ACTION_STATE_CHANGED)

@OptIn(DelicateCoroutinesApi::class)
internal val bluetoothState = broadcastReceiverFlow(intentFilter)
    .map { intent -> intent.getIntExtra(EXTRA_STATE, ERROR) }
    .shareIn(GlobalScope, started = WhileSubscribed(replayExpirationMillis = 0))
