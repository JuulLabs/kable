package com.juul.kable.bluetooth

internal suspend fun checkBluetoothIsSupported() {
    check(isSupported()) { "Bluetooth not supported" }
}
