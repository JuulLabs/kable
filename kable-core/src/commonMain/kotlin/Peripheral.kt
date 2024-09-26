@file:JvmName("PeripheralCommon")
@file:Suppress("RedundantUnitReturnType")

package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.State.Disconnecting
import com.juul.kable.WriteType.WithoutResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.JvmName

internal typealias OnSubscriptionAction = suspend () -> Unit

public enum class WriteType {
    WithResponse,
    WithoutResponse,
}

public interface Peripheral : CoroutineScope {

    /**
     * Provides a conflated [Flow] of the [Peripheral]'s [State].
     *
     * After [connect] is called, the [state] will typically transition through the following
     * [states][State]:
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
     *                               .---------------.      .--------------.
     *                               | Disconnecting | ---> | Disconnected |
     *                               '---------------'      '--------------'
     * ```
     *
     * Note that [Disconnecting] state is skipped on Apple and JavaScript when connection closure is
     * initiated by peripheral (or peripheral goes out-of-range).
     */
    public val state: StateFlow<State>

    /**
     * Platform specific identifier for the remote peripheral. On some platforms, this can be used
     * to "restore" a previously known peripheral for reconnection.
     *
     * ## Android
     *
     * On Android, this is a MAC address represented as a [String]. A [Peripheral] can be created
     * from this MAC address using the `Peripheral(String, PeripheralBuilderAction)` builder
     * function unless the peripheral makes "use of a Bluetooth Smart feature known as 'LE Privacy'"
     * (whereas the peripheral may provide a random MAC address, see
     * [Bluetooth Technology Protecting Your Privacy](https://www.bluetooth.com/blog/bluetooth-technology-protecting-your-privacy/)
     * for more details)).
     *
     * ## Apple
     *
     * On Apple, this is a unique identifier represented as a [Uuid]. A [Peripheral] can be created
     * from this identifier using the `Peripheral(Uuid, PeripheralBuilderAction)` builder function.
     * According to
     * [The Ultimate Guide to Apple’s Core Bluetooth](https://punchthrough.com/core-bluetooth-basics/):
     *
     * > This UUID isn't guaranteed to stay the same across scanning sessions and should not be 100%
     * > relied upon for peripheral re-identification. That said, we have observed it to be
     * > relatively stable and reliable over the long term assuming a major device settings reset
     * > has not occurred.
     *
     * If `Peripheral(Uuid, PeripheralBuilderAction)` throws a [NoSuchElementException] then a scan
     * will be necessary to obtain an [Advertisement] for [Peripheral] creation.
     *
     * ## JavaScript
     *
     * On JavaScript, this is a unique identifier represented as a [String]. "Restoring" a
     * peripheral from this identifier is not yet supported in Kable (as JavaScript requires user to
     * [explicitly enable this feature](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/getDevices)).
     */
    public val identifier: Identifier

    /**
     * The peripheral name, as provided by the underlying bluetooth system. This value is system
     * dependent and is not necessarily the Generic Access Profile (GAP) device name.
     *
     * This API is experimental as it may be changed to a [StateFlow] in the future (to notify of
     * name changes on Apple platform).
     */
    @ExperimentalApi
    public val name: String?

    /**
     * Initiates a connection, suspending until connected, or failure occurs. Multiple concurrent
     * invocations will all suspend until connected (or failure occurs). If already connected, then
     * returns immediately.
     *
     * The returned [CoroutineScope] can be used to launch coroutines, and is cancelled upon
     * disconnect or [Peripheral] [cancellation][Peripheral.cancel]. The [CoroutineScope] is a
     * supervisor scope, meaning any failures in launched coroutines will not fail other launched
     * coroutines nor cause a disconnect.
     *
     * @throws ConnectionRejectedException when a connection request is rejected by the system (e.g. bluetooth hardware unavailable).
     * @throws CancellationException if [Peripheral]'s [CoroutineScope] has been [cancelled][Peripheral.cancel].
     */
    public suspend fun connect(): CoroutineScope

    /**
     * Disconnects the active connection, or cancels an in-flight [connection][connect] attempt,
     * suspending until [Peripheral] has settled on a [disconnected][State.Disconnected] state.
     *
     * Multiple concurrent invocations will all suspend until disconnected (or failure occurs).
     *
     * Any coroutines launched from [connect] will be spun down prior to closing underlying
     * peripheral connection.
     *
     * @throws CancellationException if [Peripheral]'s [CoroutineScope] has been [cancelled][Peripheral.cancel].
     */
    public suspend fun disconnect(): Unit

    /**
     * The list of services (GATT profile) which have been discovered on the remote peripheral.
     *
     * The list contains a tree of [DiscoveredService]s, [DiscoveredCharacteristic]s and
     * [DiscoveredDescriptor]s. These types all hold strong references to the underlying platform
     * type, so no guarantees are provided on the validity of the objects beyond a connection. If a
     * reconnect occurs, it is recommended to retrieve the desired object from [services] again. Any
     * references to objects obtained from this tree should be cleared upon [disconnect] or disposal
     * (when [Peripheral] is [cancelled][Peripheral.cancel]).
     *
     * @return [discovered services][DiscoveredService], or `null` until services have been discovered.
     */
    public val services: StateFlow<List<DiscoveredService>?>

    /**
     * On JavaScript, requires Chrome 79+ with the
     * `chrome://flags/#enable-experimental-web-platform-features` flag enabled.
     *
     * Note that even with the above flag enabled (as of Chrome 128), RSSI is not supported and this
     * function will throw [UnsupportedOperationException].
     *
     * This API is experimental until Web Bluetooth advertisement APIs are stable.
     *
     * @throws NotConnectedException if invoked without an established [connection][connect].
     * @throws UnsupportedOperationException on JavaScript.
     */
    @ExperimentalApi
    @Throws(CancellationException::class, IOException::class)
    public suspend fun rssi(): Int

    /**
     * Reads data from [characteristic].
     *
     * If [characteristic] was created via [characteristicOf] then the first found characteristic with [Read] property
     * matching the service UUID and characteristic UUID in the GATT profile will be used. If multiple characteristics
     * with the same UUID and [Read] characteristic property exist in the GATT profile, then a
     * [discovered characteristic][DiscoveredCharacteristic] from [services] should be used instead.
     *
     * @throws NotConnectedException if invoked without an established [connection][connect].
     */
    @Throws(CancellationException::class, IOException::class)
    public suspend fun read(
        characteristic: Characteristic,
    ): ByteArray

    /**
     * Writes [data] to [characteristic].
     *
     * If [characteristic] was created via [characteristicOf] then the first found characteristic with a property
     * matching the specified [writeType] and matching the service UUID and characteristic UUID in the GATT profile will
     * be used. If multiple characteristics with the same UUID and properties exist in the GATT profile, then a
     * [discovered characteristic][DiscoveredCharacteristic] from [services] should be used instead.
     *
     * @throws NotConnectedException if invoked without an established [connection][connect].
     */
    @Throws(CancellationException::class, IOException::class)
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
     * @throws NotConnectedException if invoked without an established [connection][connect].
     */
    @Throws(CancellationException::class, IOException::class)
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
     * @throws NotConnectedException if invoked without an established [connection][connect].
     */
    @Throws(CancellationException::class, IOException::class)
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
     * While the peripheral is fully connected, failures related to observations are propagated via the returned
     * [observe] [Flow], for example, if the specified [characteristic] is invalid or cannot be found then returned
     * [Flow] terminates with a [NoSuchElementException]. An [ObservationExceptionHandler] may be registered with the
     * [Peripheral] to control which failures are propagated through (and terminate) the observation [Flow]. When
     * registered, only exceptions thrown from the [ObservationExceptionHandler] are propagated (and terminate) the
     * returned observation [Flow]. See [PeripheralBuilder.observationExceptionHandler] for more details.
     *
     * However, failures related to observations that occur during [connection][connect] are treated differently.
     * Instead of propagating through the [Flow] or the [ObservationExceptionHandler], the exception is thrown from
     * [connect] and the connection attempt fails, leaving this peripheral in the disconnected state.
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

internal typealias PeripheralBuilderAction = PeripheralBuilder.() -> Unit

public expect fun Peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral

/**
 * Suspends until [Peripheral] receiver arrives at the [State] specified.
 *
 * @see [State] for a description of the potential states.
 */
internal suspend inline fun <reified T : State> Peripheral.suspendUntil() {
    state.first { it is T }
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
        .onEach { if (it is State.Disconnected) throw NotConnectedException() }
        .first { it is T }
}
