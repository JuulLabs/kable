@file:Suppress("RedundantUnitReturnType")

package com.juul.kable

import kotlinx.coroutines.flow.Flow

public expect class Peripheral {

    public val state: Flow<State>
    public val events: Flow<Event>

    public suspend fun connect(): Unit

    public suspend fun discoverServices(): Unit

    /** @return discovered services, or `null` if services have not yet been [discovered][discoverServices]. */
    public val services: List<Service>?

    public suspend fun rssi(): Int

    public suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ): Unit

    public suspend fun read(
        characteristic: Characteristic,
    ): ByteArray

    public suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ): Unit

    public suspend fun read(
        descriptor: Descriptor,
    ): ByteArray

    public fun observe(
        characteristic: Characteristic,
    ): Flow<ByteArray>

    public suspend fun disconnect(): Unit
}
