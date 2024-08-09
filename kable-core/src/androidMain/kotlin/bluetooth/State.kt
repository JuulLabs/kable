package com.juul.kable.bluetooth

import android.bluetooth.BluetoothAdapter.ERROR
import android.bluetooth.BluetoothAdapter.EXTRA_STATE
import android.bluetooth.BluetoothAdapter.STATE_OFF
import android.bluetooth.BluetoothAdapter.STATE_ON
import android.bluetooth.BluetoothAdapter.STATE_TURNING_OFF
import android.bluetooth.BluetoothAdapter.STATE_TURNING_ON
import android.content.IntentFilter
import com.juul.kable.Bluetooth.State
import com.juul.kable.getBluetoothAdapter
import com.juul.tuulbox.coroutines.flow.broadcastReceiverFlow
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED as BLUETOOTH_STATE_CHANGED

@OptIn(DelicateCoroutinesApi::class) // For `GlobalScope`.
internal actual val state: Flow<State> =
    broadcastReceiverFlow(IntentFilter(BLUETOOTH_STATE_CHANGED))
        .map { intent -> intent.getIntExtra(EXTRA_STATE, ERROR) }
        .onStart {
            val bluetoothAdapter = getBluetoothAdapter()
            emit(if (bluetoothAdapter.isEnabled) STATE_ON else STATE_OFF)
        }
        .map { state ->
            when (state) {
                STATE_ON -> State.On
                STATE_OFF -> State.Off
                STATE_TURNING_OFF -> State.TurningOff
                STATE_TURNING_ON -> State.TurningOn
                else -> error("Unexpected bluetooth state: $state")
            }
        }
        .shareIn(GlobalScope, started = WhileSubscribed(replayExpirationMillis = 0))
