package com.juul.sensortag.bluetooth

internal object NopSystemControl : SystemControl {
    override fun showLocationSettings() {} // No-op
    override fun requestToTurnBluetoothOn() {} // No-op
}
