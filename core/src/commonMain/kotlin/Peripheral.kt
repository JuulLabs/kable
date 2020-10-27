@file:Suppress("RedundantUnitReturnType")

package com.juul.kable

import com.juul.kable.WriteType.WithoutResponse
import kotlinx.coroutines.flow.Flow

public enum class WriteType {
    WithResponse,
    WithoutResponse,
}

public expect class Peripheral {

    /**
     * Provides a [Flow] of the [Peripheral]'s [State].
     *
     * After [connect] is called, the [state] will typically transition through the following [State]s:
     *
     * ```
     *     connect()
     *         :
     *         v
     *   .------------.       .-----------.
     *   | Connecting | ----> | Connected |
     *   '------------'       '-----------'
     *                              |
     *                       connection drop
     *                              v
     *                      .---------------.       .--------------.
     *                      | Disconnecting | ----> | Disconnected |
     *                      '---------------'       '--------------'
     * ```
     *
     * This [state] [Flow] is conflated and is intended to provide current connection status. If it is desired to handle
     * specific connection events, then [events] [Flow] should be used instead.
     */
    public val state: Flow<State>

    public val events: Flow<Event>

    /**
     * Initiates a connection, suspending until connected, or failure occurs. Multiple concurrent invocations will all
     * suspend until connected (or failure). If already connected, then returns immediately.
     *
     * @throws IllegalStateException if [Peripheral]'s Coroutine scope has been cancelled.
     */
    public suspend fun connect(): Unit

    /** @return discovered [services][Service], or `null` until a [connection][connect] has been established. */
    public val services: List<DiscoveredService>?

    public suspend fun rssi(): Int

    public suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType = WithoutResponse,
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
