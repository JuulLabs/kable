@file:Suppress("RedundantUnitReturnType")

package com.juul.kable

import com.benasher44.uuid.uuidFrom
import kotlinx.coroutines.flow.Flow

public expect class Peripheral {

    public val state: Flow<State>
    public val events: Flow<Event>

    public suspend fun connect(): Unit

    public suspend fun discoverServices(): Unit

    /** @throws IllegalStateException if accessed prior to [service discovery][discoverServices]. */
    public val services: List<Service>

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

public operator fun List<Service>.get(
    uuid: String
): Service {
    val searchUuid = uuidFrom(uuid)
    return firstOrNull { it.uuid == searchUuid }
        ?: throw NoSuchElementException("Service $uuid not found.")
}
