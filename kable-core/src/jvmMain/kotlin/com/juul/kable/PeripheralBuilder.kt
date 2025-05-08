package com.juul.kable

import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder
import kotlin.time.Duration

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

    internal var logging: Logging = Logging()
    public actual fun logging(init: LoggingBuilder) {
        val logging = Logging()
        logging.init()
        this.logging = logging
    }

    internal var onServicesDiscovered: ServicesDiscoveredAction = {}
    public actual fun onServicesDiscovered(action: ServicesDiscoveredAction) {
        onServicesDiscovered = action
    }

    internal var observationExceptionHandler: ObservationExceptionHandler = { cause -> throw cause }
    public actual fun observationExceptionHandler(handler: ObservationExceptionHandler) {
        observationExceptionHandler = handler
    }

    public actual var disconnectTimeout: Duration = defaultDisconnectTimeout
}
