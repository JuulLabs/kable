package com.juul.kable

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.bluetooth.BluetoothAdapter.ERROR
import android.bluetooth.BluetoothAdapter.EXTRA_STATE
import android.bluetooth.BluetoothAdapter.STATE_OFF
import android.bluetooth.BluetoothAdapter.STATE_ON
import android.bluetooth.BluetoothAdapter.STATE_TURNING_OFF
import android.bluetooth.BluetoothAdapter.STATE_TURNING_ON
import android.content.IntentFilter
import com.juul.kable.Bluetooth.Availability.Available
import com.juul.kable.Bluetooth.Availability.Unavailable
import com.juul.kable.Reason.Off
import com.juul.kable.Reason.TurningOff
import com.juul.kable.Reason.TurningOn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

public actual enum class Reason {
    Off, // BluetoothAdapter.STATE_OFF
    TurningOff, // BluetoothAdapter.STATE_TURNING_OFF or BluetoothAdapter.STATE_BLE_TURNING_OFF
    TurningOn, // BluetoothAdapter.STATE_TURNING_ON or BluetoothAdapter.STATE_BLE_TURNING_ON
}

internal actual val bluetoothAvailability: Flow<Bluetooth.Availability> =
    broadcastReceiverFlow(IntentFilter(ACTION_STATE_CHANGED))
        .map { intent -> intent.getIntExtra(EXTRA_STATE, ERROR) }
        .onStart {
            val state = when (BluetoothAdapter.getDefaultAdapter()?.isEnabled) {
                true -> STATE_ON
                else -> STATE_OFF
            }
            emit(state)
        }
        .map { state ->
            when (state) {
                STATE_ON -> Available
                STATE_OFF -> Unavailable(reason = Off)
                STATE_TURNING_OFF -> Unavailable(reason = TurningOff)
                STATE_TURNING_ON -> Unavailable(reason = TurningOn)
                else -> error("Unexpected bluetooth state: $state")
            }
        }
