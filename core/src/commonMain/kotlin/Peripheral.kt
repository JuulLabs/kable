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

    /**
     * The list of services (GATT profile) which have been discovered on the remote peripheral.
     *
     * The list contains a tree of [DiscoveredService]s, [DiscoveredCharacteristic]s and [DiscoveredDescriptor]s. These
     * types all hold strong references to the underlying platform type, so no guarantees are provided on the validity
     * of the objects beyond a connection. If a reconnect occurs, it is recommended to retrieve the desired object from
     * [services] again. Any references to objects obtained from this tree should be cleared upon disconnect or disposal
     * (when parent [CoroutineScope] is cancelled) of this [Peripheral].
     *
     * @return [discovered services][DiscoveredService], or `null` until a [connection][connect] has been established.
     */
    public val services: List<DiscoveredService>?



    /** @throws NotReadyException if invoked without an established [connection][connect]. */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun rssi(): Int

    /**
     * Reads data from [characteristic].
     *
     * If [characteristic] was created via [characteristicOf] then the first found characteristic with [Read] property
     * matching the service UUID and characteristic UUID in the GATT profile will be used. If multiple characteristics
     * with the same UUID and [Read] characteristic property exist in the GATT profile, then a
     * [discovered characteristic][DiscoveredCharacteristic] from [services] should be used instead.
     *
     * @throws NotReadyException if invoked without an established [connection][connect].
     */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun read(
        characteristic: Characteristic,
    ): ByteArray

    /**
     * Writes [data] to [characteristic].
     *
     * If [characteristic] was created via [characteristicOf] then the first found characteristic with a property
     * matching the specified [writeType] and matching the service UUID and characteristic UUID in the GATT profile will
     * be used. If multiple characteristics with the same UUID and property exist in the GATT profile, then a
     * [discovered characteristic][DiscoveredCharacteristic] from [services] should be used instead.
     *
     * @throws NotReadyException if invoked without an established [connection][connect].
     */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType = WithoutResponse,
    ): Unit

    /**
     * Reads data from [descriptor].
     *
     * If [descriptor] was created via [descriptorOf] then the first found descriptor (matching the service UUID,
     * characteristic UUID and descriptor UUID) in the GATT profile will be used. If multiple descriptors with the same
     * UUID exist in the GATT profile, then a [discovered descriptor][DiscoveredDescriptor] from [services] should be
     * used instead.
     *
     * @throws NotReadyException if invoked without an established [connection][connect].
     */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun read(
        descriptor: Descriptor,
    ): ByteArray

    /**
     * Writes [data] to [descriptor].
     *
     * If [descriptor] was created via [descriptorOf] then the first found descriptor (matching the service UUID,
     * characteristic UUID and descriptor UUID) in the GATT profile will be used. If multiple descriptors with the same
     * UUID exist in the GATT profile, then a [discovered descriptor][DiscoveredDescriptor] from [services] should be
     * used instead.
     *
     * @throws NotReadyException if invoked without an established [connection][connect].
     */
    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ): Unit

    /**
     * Observes changes to the specified [Characteristic].
     *
     * If [characteristic] was created via [characteristicOf] then the first found characteristic with a property of
     * **notify** or **indicate** and matching service UUID and characteristic UUID will be used. If multiple
     * characteristics with the same UUID and either **notify** or **indicate** property exist in the GATT profile, then
     * a [discovered characteristic][DiscoveredCharacteristic] from [services] should be used instead.
     *
     * When using [characteristicOf], observations can be setup ([observe] can be called) prior to a
     * [connection][connect] being established. Once connected, the observation will automatically start emitting
     * changes. If connection is lost, [Flow] will remain active, once reconnected characteristic changes will begin
     * emitting again.
     *
     * If characteristic has a Client Characteristic Configuration descriptor (CCCD), then based on bits in the
     * [characteristic] properties, observe will be configured (CCCD will be written to) as **notification** or
     * **indication** (if [characteristic] supports both notifications and indications, then only **notification** is
     * used).
     *
     * Failures related to observations are propagated via the returned [observe] [Flow], for example, if the specified
     * [characteristic] is invalid or cannot be found then returned [Flow] terminates with a [NoSuchElementException].
     * An [ObservationExceptionHandler] may be registered with the [Peripheral] to control which failures are propagated
     * through (and terminate) the observation [Flow]. When registered, only exceptions thrown from the
     * [ObservationExceptionHandler] are propagated (and terminate) the returned observation [Flow]. See
     * [PeripheralBuilder.observationExceptionHandler] for more details.
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

/**
 * The identifier of the remote device
 *
 * This identifier is the identifier returned by the operating system for the address of the device.
 * The value of the identifier will differ between operating system implementations.  On iOS
 * the identifier is a UUID.  On Android the identifier is a MAC address.  For security reasons
 * the identifier is not a MAC address on iOS and may be a randomized MAC on Android.  The identifier
 * may also change.
 */
public expect val Peripheral.identifier: String
