@file:JvmName("PeripheralCommon")
@file:Suppress("RedundantUnitReturnType")

package com.juul.kable

import com.juul.kable.WriteType.WithoutResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.JvmName

internal typealias PeripheralBuilderAction = PeripheralBuilder.() -> Unit
internal typealias OnSubscriptionAction = suspend () -> Unit

public expect fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction = {},
): Peripheral

public enum class WriteType {
    WithResponse,
    WithoutResponse,
}

public interface Peripheral {

    /**
     * Provides a conflated [Flow] of the [Peripheral]'s [State].
     *
     * After [connect] is called, the [state] will typically transition through the following [states][State]:
     *
     * ```
     *           connect()
     *               :
     *               v
     *   .----------------------.
     *   | Connecting.Bluetooth |
     *   '----------------------'
     *               |
     *               v
     *    .---------------------.
     *    | Connecting.Services |
     *    '---------------------'
     *               |
     *               v
     *    .---------------------.      .-----------.
     *    | Connecting.Observes | ---> | Connected |
     *    '---------------------'      '-----------'
     *                                       :
     *                                disconnect() or
     *                                connection drop
     *                                       :
     *                                       v
     *                               .---------------.       .--------------.
     *                               | Disconnecting | ----> | Disconnected |
     *                               '---------------'       '--------------'
     * ```
     */
    public val state: StateFlow<State>

    /**
     * Initiates a connection, suspending until connected, or failure occurs. Multiple concurrent invocations will all
     * suspend until connected (or failure occurs). If already connected, then returns immediately.
     *
     * @throws ConnectionRejectedException when a connection request is rejected by the system (e.g. bluetooth hardware unavailable).
     * @throws CancellationException if [Peripheral]'s Coroutine scope has been cancelled.
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

    /** @throws NotReadyException if invoked without an established [connection][connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun rssi(): Int

    /** @throws NotReadyException if invoked without an established [connection][connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun read(
        characteristic: Characteristic,
    ): ByteArray

    /** @throws NotReadyException if invoked without an established [connection][connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType = WithoutResponse,
    ): Unit

    /** @throws NotReadyException if invoked without an established [connection][connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun read(
        descriptor: Descriptor,
    ): ByteArray

    /** @throws NotReadyException if invoked without an established [connection][connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
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
     * If characteristic has a Client Characteristic Configuration descriptor (CCCD), then based on bits in the
     * [characteristic] properties, observe will be configured (CCCD will be written to) as **notification** or
     * **indication** (if [characteristic] supports both notifications and indications, then only **notification** is
     * used).
     *
     * Failures related to notifications are propagated via the returned [observe] [Flow], for example, if the specified
     * [characteristic] is invalid or cannot be found then a [NoSuchElementException] is propagated via the returned
     * [Flow].
     *
     * The optional [onSubscription] parameter is functionally identical to using the
     * [onSubscription][kotlinx.coroutines.flow.onSubscription] operator on the returned [Flow] except it has the
     * following special properties:
     *
     * - It will be executed whenever [connection][connect] is established (while the returned [Flow] is active); and
     * - It will be executed _after_ the observation is spun up (i.e. after enabling notifications or indications)
     *
     * The [onSubscription] action is useful in situations where an initial operation is needed when starting an
     * observation (such as writing a configuration to the peripheral and expecting the response to come back in the
     * form of a characteristic change). The [onSubscription] is invoked for every new subscriber; if it is desirable to
     * only invoke the [onSubscription] once per connection (for the specified [characteristic]) then you can either
     * use the [shareIn][kotlinx.coroutines.flow.shareIn] [Flow] operator on the returned [Flow], or call [observe]
     * again with the same [characteristic] and without specifying an [onSubscription] action.
     *
     * If multiple [observations][observe] are created for the same [characteristic] but with different [onSubscription]
     * actions, then the [onSubscription] actions will be executed in the order in which the returned [Flow]s are
     * subscribed to.
     */
    public fun observe(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction = {},
    ): Flow<ByteArray>
}

/**
 * Suspends until [Peripheral] receiver arrives at the [State] specified.
 *
 * @see [State] for a description of the potential states.
 */
internal suspend inline fun <reified T : State> Peripheral.suspendUntil() {
    state.first { it is T }
}

/**
 * Suspends until [Peripheral] receiver arrives at the [State] specified or any [State] above it.
 *
 * @see [State] for a description of the potential states.
 * @see [State.isAtLeast] for state ordering.
 */
internal suspend inline fun <reified T : State> Peripheral.suspendUntilAtLeast() {
    state.first { it.isAtLeast<T>() }
}

/**
 * Suspends until [Peripheral] receiver arrives at the [State] specified.
 *
 * @see State for a description of the potential states.
 * @throws ConnectionLostException if peripheral state arrives at [State.Disconnected].
 */
internal suspend inline fun <reified T : State> Peripheral.suspendUntilOrThrow() {
    require(T::class != State.Disconnected::class) {
        "Peripheral.suspendUntilOrThrow() throws on State.Disconnected, not intended for use with that State."
    }
    state
        .onEach { if (it is State.Disconnected) throw ConnectionLostException() }
        .first { it is T }
}

internal expect val Peripheral.identifier: String
