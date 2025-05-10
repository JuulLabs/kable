package com.juul.kable

import com.juul.kable.btleplug.BtleplugPeripheral
import kotlinx.coroutines.Dispatchers

internal actual fun Peripheral.observationHandler(): Observation.Handler {
    check(this is BtleplugPeripheral) { "Peripheral is not a BtleplugPeripheral" }
    return BtleplugObservationHandler(this)
}

private class BtleplugObservationHandler(
    val peripheral: BtleplugPeripheral,
) : Observation.Handler {
    override suspend fun startObservation(characteristic: Characteristic) {
        peripheral.logger.info { "Start observation: $characteristic" }
        val ffi = peripheral.peripheral.await()
        with(Dispatchers.IO) {
            ffi.subscribe(peripheral.getCharacteristic(characteristic))
        }
    }

    override suspend fun stopObservation(characteristic: Characteristic) {
        peripheral.logger.info { "Stop observation: $characteristic" }
        val ffi = peripheral.peripheral.await()
        with(Dispatchers.IO) {
            ffi.unsubscribe(peripheral.getCharacteristic(characteristic))
        }
    }

}
