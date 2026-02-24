package com.juul.kable

internal actual fun Peripheral.observationHandler(): Observation.Handler = object : Observation.Handler {
    override suspend fun startObservation(characteristic: Characteristic) {
        (this@observationHandler as BluetoothDeviceWebBluetoothPeripheral).startObservation(characteristic)
    }

    override suspend fun stopObservation(characteristic: Characteristic) {
        (this@observationHandler as BluetoothDeviceWebBluetoothPeripheral).stopObservation(characteristic)
    }
}
