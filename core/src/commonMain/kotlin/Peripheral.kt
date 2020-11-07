@file:Suppress("RedundantUnitReturnType")

package com.juul.kable

import com.juul.kable.WriteType.WithoutResponse
import kotlinx.coroutines.flow.Flow

public enum class WriteType {
    WithResponse,
    WithoutResponse,
}

public interface Peripheral {

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
     *                              :
     *                       disconnect() or
     *                       connection drop
     *                              :
     *                              v
     *                      .---------------.       .--------------.
     *                      | Disconnecting | ----> | Disconnected |
     *                      '---------------'       '--------------'
     * ```
     *
     * The [state] [Flow] is conflated (so some states may be missed by slow consumers); it is intended to provide the
     * current connection status to the user. If it is desired to handle (take action based on) specific connection
     * events, then [events] [Flow] should be used instead.
     */
    public val state: Flow<State>

    public val events: Flow<Event>

    /**
     * Initiates a connection, suspending until connected, or failure occurs. Multiple concurrent invocations will all
     * suspend until connected (or failure occurs). If already connected, then returns immediately.
     *
     * @throws IllegalStateException if [Peripheral]'s Coroutine scope has been cancelled.
     */
    public suspend fun connect(): Unit

    /** @return discovered [services][Service], or `null` until a [connection][connect] has been established. */
    public val services: List<DiscoveredService>?

    public suspend fun rssi(): Int

    /**
     * @throws NotReadyException if invoked without an established [connection][connect].
     */
    public suspend fun read(
        characteristic: Characteristic,
    ): ByteArray

    /**
     * @throws NotReadyException if invoked without an established [connection][connect].
     */
    public suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType = WithoutResponse,
    ): Unit

    /**
     * @throws NotReadyException if invoked without an established [connection][connect].
     */
    public suspend fun read(
        descriptor: Descriptor,
    ): ByteArray

    /**
     * @throws NotReadyException if invoked without an established [connection][connect].
     */
    public suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ): Unit

    /**
     * Observes changes to the specified [Characteristic].
     *
     * Observations can be setup ([observe] can be called) prior to a [connection][connect] being established. Once
     * connected, the observation will automatically start emitting changes. If connection is lost, [Flow] will remain
     * active, once reconnected characteristic changes will begin emitting again.
     *
     * If the specified [characteristic] is invalid or cannot be found then a [NoSuchElementException] will be
     * propagated via the [connect] function.
     */
    public fun observe(
        characteristic: Characteristic,
    ): Flow<ByteArray>

    /**
     * Disconnects the active connection, or cancels an in-flight [connection][connect] attempt, suspending until
     * [Peripheral] has settled on a [disconnected][State.Disconnected] state.
     *
     * Multiple concurrent invocations will all suspend until disconnected (or failure).
     */
    public suspend fun disconnect(): Unit
}
