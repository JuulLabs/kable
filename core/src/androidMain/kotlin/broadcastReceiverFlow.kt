package com.juul.kable

import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import com.juul.tuulbox.coroutines.flow.broadcastReceiverFlow
import kotlinx.coroutines.flow.Flow

@Deprecated(
    message = "Now provided by Tuulbox and no longer maintained as part of Kable.",
    replaceWith = ReplaceWith("com.juul.tuulbox.coroutines.flow.broadcastReceiverFlow"),
    level = DeprecationLevel.WARNING,
)
public fun broadcastReceiverFlow(
    intentFilter: IntentFilter,
): Flow<Intent> = broadcastReceiverFlow(intentFilter, RECEIVER_NOT_EXPORTED)
