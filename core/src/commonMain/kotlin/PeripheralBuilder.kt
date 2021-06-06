package com.juul.kable

import kotlin.coroutines.cancellation.CancellationException

public expect class OnConnectPeripheral {

    /**
     * @throws NotReadyException if invoked without an established [connection][Peripheral.connect].
     */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun read(
        characteristic: Characteristic,
    ): ByteArray

    /**
     * @throws NotReadyException if invoked without an established [connection][Peripheral.connect].
     */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType = WriteType.WithoutResponse,
    ): Unit

    /**
     * @throws NotReadyException if invoked without an established [connection][Peripheral.connect].
     */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun read(
        descriptor: Descriptor,
    ): ByteArray

    /**
     * @throws NotReadyException if invoked without an established [connection][Peripheral.connect].
     */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ): Unit
}

internal typealias OnConnectAction = suspend OnConnectPeripheral.() -> Unit

public expect class PeripheralBuilder internal constructor() {
    public fun onConnect(action: OnConnectAction)
}
