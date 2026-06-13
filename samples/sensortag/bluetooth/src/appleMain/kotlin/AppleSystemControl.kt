package com.juul.sensortag.bluetooth

import platform.CoreBluetooth.CBCentralManager

internal class AppleSystemControl : SystemControl {

    override fun showLocationSettings() {
        // No-op
    }

    override fun requestToTurnBluetoothOn() {
        // When `options` are not provided, then `CBCentralManagerOptionShowPowerAlertKey` defaults
        // to `true` (which shows the "turn on bluetooth" dialog).
        CBCentralManager()
    }
}
