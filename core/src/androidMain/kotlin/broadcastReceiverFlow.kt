package com.juul.kable

import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import com.juul.tuulbox.coroutines.flow.broadcastReceiverFlow
import kotlinx.coroutines.flow.Flow

@Deprecated("Was not intended to be public. Will not be available (will be `internal`) in a future release.")
public fun broadcastReceiverFlow(
    intentFilter: IntentFilter,
): Flow<Intent> = broadcastReceiverFlow(intentFilter, RECEIVER_NOT_EXPORTED)
