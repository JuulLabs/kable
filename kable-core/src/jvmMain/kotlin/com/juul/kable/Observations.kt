package com.juul.kable

internal actual fun Peripheral.observationHandler(): Observation.Handler = object : Observation.Handler {
    override suspend fun startObservation(characteristic: Characteristic) {
        jvmNotImplementedException()
    }

    override suspend fun stopObservation(characteristic: Characteristic) {
        jvmNotImplementedException()
    }
}
