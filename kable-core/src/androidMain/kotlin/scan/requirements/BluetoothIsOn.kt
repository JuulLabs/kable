package com.juul.kable.scan.requirements

import android.bluetooth.BluetoothAdapter.STATE_OFF
import android.bluetooth.BluetoothAdapter.STATE_ON
import android.bluetooth.BluetoothAdapter.STATE_TURNING_OFF
import android.bluetooth.BluetoothAdapter.STATE_TURNING_ON
import com.juul.kable.InternalException
import com.juul.kable.UnmetRequirementException
import com.juul.kable.UnmetRequirementReason.BluetoothDisabled
import com.juul.kable.getBluetoothAdapter

/**
 * @throws IllegalStateException If bluetooth is not supported.
 * @throws UnmetRequirementException If bluetooth adapter state is not [STATE_ON].
 */
internal fun checkBluetoothIsOn() {
    val actual = getBluetoothAdapter().state
    val expected = STATE_ON
    if (actual != expected) {
        throw UnmetRequirementException(
            reason = BluetoothDisabled,
            message = "Bluetooth was ${nameFor(actual)}, but ${nameFor(expected)} was required",
        )
    }
}

private fun nameFor(state: Int) = when (state) {
    STATE_OFF -> "STATE_OFF"
    STATE_ON -> "STATE_ON"
    STATE_TURNING_OFF -> "STATE_TURNING_OFF"
    STATE_TURNING_ON -> "STATE_TURNING_ON"
    else -> throw InternalException("Unsupported bluetooth state: $state")
}
