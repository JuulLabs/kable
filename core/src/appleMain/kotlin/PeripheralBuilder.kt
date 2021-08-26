package com.juul.kable

import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder
import kotlin.coroutines.cancellation.CancellationException

public actual class ServicesDiscoveredPeripheral internal constructor(
    private val peripheral: ApplePeripheral
) {

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public actual suspend fun read(
        characteristic: Characteristic,
    ): ByteArray = peripheral.read(characteristic)

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public actual suspend fun read(
        descriptor: Descriptor,
    ): ByteArray = peripheral.read(descriptor)

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public actual suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ) {
        peripheral.write(characteristic, data, writeType)
    }

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public actual suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ) {
        peripheral.write(descriptor, data)
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
}
