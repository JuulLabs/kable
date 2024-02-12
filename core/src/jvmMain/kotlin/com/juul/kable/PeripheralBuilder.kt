package com.juul.kable

import com.juul.kable.logs.LoggingBuilder

public actual class ServicesDiscoveredPeripheral internal constructor() {

    public actual suspend fun read(
        characteristic: Characteristic,
    ): ByteArray = jvmNotImplementedException()

    public actual suspend fun read(
        descriptor: Descriptor,
    ): ByteArray = jvmNotImplementedException()

    public actual suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ) {
        jvmNotImplementedException()
    }

    public actual suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ) {
        jvmNotImplementedException()
    }
}

public actual class PeripheralBuilder internal actual constructor() {

    public actual fun logging(init: LoggingBuilder) {
        jvmNotImplementedException()
    }

    public actual fun onServicesDiscovered(action: ServicesDiscoveredAction) {
        jvmNotImplementedException()
    }

    public actual fun observationExceptionHandler(handler: ObservationExceptionHandler) {
        jvmNotImplementedException()
    }
}
