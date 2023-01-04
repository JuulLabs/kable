package com.juul.kable

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

public fun broadcastReceiverFlow(
    intentFilter: IntentFilter,
): Flow<Intent> = callbackFlow {
    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) trySend(intent)
        }
    }
    ContextCompat.registerReceiver(applicationContext, broadcastReceiver, intentFilter, 0)
    awaitClose { applicationContext.unregisterReceiver(broadcastReceiver) }
}
