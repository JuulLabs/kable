package com.juul.kable

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Deprecated("Was not intended to be public. Will not be available (will be `internal`) in a future release.")
public fun broadcastReceiverFlow(
    intentFilter: IntentFilter,
): Flow<Intent> = callbackFlow {
    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) trySend(intent)
        }
    }
    ContextCompat.registerReceiver(applicationContext, broadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
    awaitClose { applicationContext.unregisterReceiver(broadcastReceiver) }
}
