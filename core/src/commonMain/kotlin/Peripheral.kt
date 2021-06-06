@file:JvmName("PeripheralCommon")
@file:Suppress("RedundantUnitReturnType")

package com.juul.kable

import com.juul.kable.WriteType.WithoutResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.JvmName

internal typealias PeripheralBuilderAction = PeripheralBuilder.() -> Unit

public expect fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction = {},
): Peripheral

public enum class WriteType {
    WithResponse,
    WithoutResponse,
}

public interface Peripheral : PeripheralIo {

    /**
     * Provides a conflated [Flow] of the [Peripheral]'s [State].
     *
     * After [connect] is called, the [state] will typically transition through the following [states][State]:
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
     */
    public val state: Flow<State>

    /**
     * Initiates a connection, suspending until connected, or failure occurs. Multiple concurrent invocations will all
     * suspend until connected (or failure occurs). If already connected, then returns immediately.
     *
     * @throws ConnectionRejectedException when a connection request is rejected by the system (e.g. bluetooth hardware unavailable).
     * @throws IllegalStateException if [Peripheral]'s Coroutine scope has been cancelled.
     */
    public suspend fun connect(): Unit

    /**
     * Disconnects the active connection, or cancels an in-flight [connection][connect] attempt, suspending until
     * [Peripheral] has settled on a [disconnected][State.Disconnected] state.
     *
     * Multiple concurrent invocations will all suspend until disconnected (or failure occurs).
     */
    public suspend fun disconnect(): Unit

    /** @return discovered [services][Service], or `null` until a [connection][connect] has been established. */
    public val services: List<DiscoveredService>?

    /**
     * Observes changes to the specified [Characteristic].
     *
     * Observations can be setup ([observe] can be called) prior to a [connection][connect] being established. Once
     * connected, the observation will automatically start emitting changes. If connection is lost, [Flow] will remain
     * active, once reconnected characteristic changes will begin emitting again.
     *
     * If characteristic has a Client Characteristic Configuration descriptor (CCCD), then based on bits in the
     * [characteristic] properties, observe will be configured (CCCD will be written to) as **notification** and/or
     * **indication**.
     *
     * Failures related to notifications are propagated via [connect] if the [observe] [Flow] is collected prior to a
     * connection being established. If a connection is already established when an [observe] [Flow] collection begins,
     * then notification failures are propagated via the returned [observe] [Flow].
     *
     * If the specified [characteristic] is invalid or cannot be found then a [NoSuchElementException] is propagated.
     */
    public fun observe(
        characteristic: Characteristic,
    ): Flow<ByteArray>
}

public expect interface PeripheralIo : PeripheralIoCommon

public interface PeripheralIoCommon {

    /**
     * @throws NotReadyException if invoked without an established [connection][Peripheral.connect].
     */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun rssi(): Int

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
        writeType: WriteType = WithoutResponse,
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
