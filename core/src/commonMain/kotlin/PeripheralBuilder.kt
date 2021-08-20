package com.juul.kable

import com.juul.kable.logs.LoggingBuilder

public expect class ServicesDiscoveredPeripheral {

    public suspend fun read(
        characteristic: Characteristic,
    ): ByteArray

    public suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType = WriteType.WithoutResponse,
    ): Unit

    public suspend fun read(
        descriptor: Descriptor,
    ): ByteArray

    public suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ): Unit
}

internal typealias ServicesDiscoveredAction = suspend ServicesDiscoveredPeripheral.() -> Unit

public expect class PeripheralBuilder internal constructor() {
    public fun logging(init: LoggingBuilder)
    public fun onServicesDiscovered(action: ServicesDiscoveredAction)
}
