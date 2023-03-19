package com.juul.kable

internal actual fun Peripheral.observationHandler(): Observation.Handler = object : Observation.Handler {
    override suspend fun startObservation(characteristic: Characteristic) {
        (this@observationHandler as CBPeripheralCoreBluetoothPeripheral).startNotifications(characteristic)
    }

    override suspend fun stopObservation(characteristic: Characteristic) {
        (this@observationHandler as CBPeripheralCoreBluetoothPeripheral).stopNotifications(characteristic)
    }
}
