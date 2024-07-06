package com.juul.kable

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import androidx.core.content.ContextCompat

private fun getBluetoothManagerOrNull(): BluetoothManager? =
    ContextCompat.getSystemService(applicationContext, BluetoothManager::class.java)

private fun getBluetoothManager(): BluetoothManager =
    getBluetoothManagerOrNull() ?: error("BluetoothManager is not a supported system service.")

/**
 * Per documentation, `BluetoothAdapter.getDefaultAdapter()` returns `null` when "Bluetooth is not
 * supported on this hardware platform".
 *
 * https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#getDefaultAdapter()
 */
internal fun getBluetoothAdapterOrNull(): BluetoothAdapter? =
    getBluetoothManager().adapter

internal fun getBluetoothAdapter(): BluetoothAdapter =
    getBluetoothAdapterOrNull() ?: error("Bluetooth not supported")

/**
 * Explicitly check the adapter state before connecting in order to respect system settings.
 * Android doesn't actually turn bluetooth off when the setting is disabled, so without this
 * check we're able to reconnect the device illegally.
 */
internal fun checkBluetoothAdapterState(
    expected: Int,
) {
    fun nameFor(value: Int) = when (value) {
        BluetoothAdapter.STATE_OFF -> "Off"
        BluetoothAdapter.STATE_ON -> "On"
        BluetoothAdapter.STATE_TURNING_OFF -> "TurningOff"
        BluetoothAdapter.STATE_TURNING_ON -> "TurningOn"
        else -> "Unknown"
    }
    val actual = getBluetoothAdapter().state
    if (expected != actual) {
        val actualName = nameFor(actual)
        val expectedName = nameFor(expected)
        throw BluetoothDisabledException("Bluetooth adapter state is $actualName ($actual), but $expectedName ($expected) was required.")
    }
}
