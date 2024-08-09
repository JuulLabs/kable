package com.juul.kable

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import androidx.core.content.ContextCompat
import com.juul.kable.UnmetRequirementReason.BluetoothDisabled

private fun getBluetoothManagerOrNull(): BluetoothManager? =
    ContextCompat.getSystemService(applicationContext, BluetoothManager::class.java)

/** @throws IllegalStateException If bluetooth is unavailable. */
private fun getBluetoothManager(): BluetoothManager =
    getBluetoothManagerOrNull() ?: error("BluetoothManager is not a supported system service")

/**
 * Per documentation, `BluetoothAdapter.getDefaultAdapter()` returns `null` when "Bluetooth is not
 * supported on this hardware platform".
 *
 * https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#getDefaultAdapter()
 */
internal fun getBluetoothAdapterOrNull(): BluetoothAdapter? =
    getBluetoothManagerOrNull()?.adapter

/** @throws IllegalStateException If bluetooth is not supported. */
internal fun getBluetoothAdapter(): BluetoothAdapter =
    getBluetoothManager().adapter ?: error("Bluetooth not supported")

/**
 * Explicitly check the adapter state before connecting in order to respect system settings.
 * Android doesn't actually turn bluetooth off when the setting is disabled, so without this
 * check we're able to reconnect the device illegally.
 *
 * @throws IllegalStateException If bluetooth is not supported.
 * @throws UnmetRequirementException In bluetooth adapter is in an unexpected state.
 */
internal fun checkBluetoothAdapterState(expected: Int) {
    val actual = getBluetoothAdapter().state
    if (expected != actual) {
        val actualName = nameFor(actual)
        val expectedName = nameFor(expected)
        throw UnmetRequirementException(
            reason = BluetoothDisabled,
            message = "Bluetooth adapter state is $actualName ($actual), but $expectedName ($expected) was required.",
        )
    }
}

private fun nameFor(state: Int) = when (state) {
    BluetoothAdapter.STATE_OFF -> "Off"
    BluetoothAdapter.STATE_ON -> "On"
    BluetoothAdapter.STATE_TURNING_OFF -> "TurningOff"
    BluetoothAdapter.STATE_TURNING_ON -> "TurningOn"
    else -> "Unknown"
}
