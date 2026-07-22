package com.juul.kable

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import androidx.core.content.ContextCompat.RegisterReceiverFlags
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Copied from Tuulbox.
// https://github.com/JuulLabs/tuulbox/blob/8.1.0/coroutines/src/androidMain/kotlin/flow/BroadcastReceiverFlow.kt
internal fun broadcastReceiverFlow(
    intentFilter: IntentFilter,
    @RegisterReceiverFlags flags: Int = RECEIVER_NOT_EXPORTED,
): Flow<Intent> = callbackFlow {
    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            trySend(intent)
        }
    }
    ContextCompat.registerReceiver(applicationContext, broadcastReceiver, intentFilter, flags)
    awaitClose { applicationContext.unregisterReceiver(broadcastReceiver) }
}
