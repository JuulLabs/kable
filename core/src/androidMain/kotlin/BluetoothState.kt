package com.juul.kable

import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.content.IntentFilter
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import com.juul.tuulbox.coroutines.flow.broadcastReceiverFlow
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.shareIn

private val intentFilter = IntentFilter(ACTION_STATE_CHANGED)

@OptIn(DelicateCoroutinesApi::class)
internal val bluetoothState = broadcastReceiverFlow(intentFilter, RECEIVER_NOT_EXPORTED)
    .shareIn(GlobalScope, started = WhileSubscribed(replayExpirationMillis = 0))
