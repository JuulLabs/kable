package com.juul.kable.bluetooth

import android.bluetooth.BluetoothDevice

private const val ZERO_MAC_ADDRESS = "00:00:00:00:00:00"

/** Performs the same MAC address validation as performed in [BluetoothDevice.connectGatt]. */
internal fun requireNonZeroAddress(address: String): String {
    require(ZERO_MAC_ADDRESS != address) { "Invalid address: $address" }
    return address
}
