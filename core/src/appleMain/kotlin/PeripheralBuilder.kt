package com.juul.kable

import kotlin.coroutines.cancellation.CancellationException

public actual class OnConnectPeripheral internal constructor(
    private val peripheral: ApplePeripheral
) {

    /** @throws NotReadyException if invoked without an established [connection][Peripheral.connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public actual suspend fun read(
        characteristic: Characteristic,
    ): ByteArray = peripheral.read(characteristic)

    /** @throws NotReadyException if invoked without an established [connection][Peripheral.connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public actual suspend fun read(
        descriptor: Descriptor,
    ): ByteArray = peripheral.read(descriptor)

    /** @throws NotReadyException if invoked without an established [connection][Peripheral.connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public actual suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ) {
        peripheral.write(characteristic, data, writeType)
    }

    /** @throws NotReadyException if invoked without an established [connection][Peripheral.connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public actual suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ) {
        peripheral.write(descriptor, data)
    }
}

public actual class PeripheralBuilder internal actual constructor() {

    internal var onConnect: OnConnectAction = {}
    public actual fun onConnect(action: OnConnectAction) {
        onConnect = action
    }
}
