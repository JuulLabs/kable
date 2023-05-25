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
