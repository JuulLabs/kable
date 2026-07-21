package com.juul.kable.server

import com.juul.kable.Characteristic
import com.juul.kable.ExperimentalKableApi
import com.juul.kable.characteristicOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents the local device acting in the Bluetooth Low Energy peripheral role: hosting a GATT
 * server (a tree of services, characteristics and descriptors) that remote [centrals][Central] can
 * connect to and interact with.
 *
 * A [GattServer] is created via the [GattServer] builder function, whereas the GATT profile
 * (services, characteristics and descriptors) and the behavior of its characteristics and
 * descriptors are declared via the builder DSL:
 *
 * ```
 * val server = GattServer {
 *     service(Uuid.service("heart_rate")) {
 *         characteristic(Uuid.characteristic("heart_rate_measurement")) {
 *             onSubscription {
 *                 while (true) {
 *                     send(measureHeartRate())
 *                     delay(1.seconds)
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * The declared profile is published (via [start]) and advertised (via [advertise]) as follows:
 *
 * ```
 * server.start()
 * server.advertise {
 *     name = "Example"
 *     services = listOf(Uuid.service("heart_rate"))
 * }
 * ```
 *
 * Should be disposed (via [close]) when no longer needed.
 */
@ExperimentalKableApi
public interface GattServer : AutoCloseable {

    public sealed class State {

        /**
         * [GattServer] is not running (either it was never [started][start], it was
         * [stopped][stop], or it failed to start).
         *
         * @param cause of failure that resulted in this state, or `null` when stopped normally.
         */
        public data class Stopped(val cause: Exception? = null) : State()

        /** [GattServer] is being started (services are being published). */
        public data object Starting : State()

        /** [GattServer] is running (services are published and available to remote centrals). */
        public data object Started : State()

        /** [GattServer] is being stopped (spinning down subscriptions and unpublishing services). */
        public data object Stopping : State()
    }

    /** Provides a [StateFlow] of the [GattServer]'s [State]. */
    public val state: StateFlow<State>

    /**
     * Remote [centrals][Central] currently connected to this server.
     *
     * On Apple, Core Bluetooth does not provide connection events for the peripheral role, so
     * [centrals] is best-effort: a [Central] is known while it is subscribed to at least one
     * characteristic (i.e. this flow is derived from subscription events).
     */
    public val centrals: StateFlow<Set<Central>>

    /**
     * Starts the GATT server, suspending until all services (declared via the [GattServer] builder
     * DSL) have been published. If already started, returns immediately.
     *
     * The returned [CoroutineScope] can be used to launch coroutines that should run for the
     * duration of the server session, and is cancelled upon [stop] or [closure][close]. The
     * [CoroutineScope] is a supervisor scope, meaning any failures in launched coroutines will not
     * fail other launched coroutines nor stop the server.
     *
     * @throws IllegalStateException if this [GattServer] has been [closed][close] or the platform GATT server could not be opened (e.g. bluetooth unsupported, disabled, or permission denied).
     */
    public suspend fun start(): CoroutineScope

    /**
     * Stops the GATT server: cancels active [subscriptions][CharacteristicBuilder.onSubscription]
     * and [advertising][advertise], unpublishes services, and suspends until the server has been
     * spun down. Does nothing if not [started][start].
     *
     * The [GattServer] may be [started][start] again after being stopped.
     */
    public suspend fun stop()

    /**
     * Advertises this peripheral, suspending while advertising is active: advertising is stopped
     * when the coroutine that invoked [advertise] is cancelled. Returns normally if the server is
     * [stopped][stop] (or [closed][close]) while advertising.
     *
     * The data to advertise is configured via [builderAction]; by default (empty [builderAction])
     * a connectable advertisement without a local name nor service UUIDs is broadcast. Platform
     * specific advertisement parameters are available on the platform-specific
     * [AdvertisementParametersBuilder]s.
     *
     * Advertising is on a "best effort" basis: advertisement data is limited in size (e.g. 28 bytes
     * on Apple while the app is in the foreground) and platforms may omit or truncate data that
     * does not fit (e.g. on Apple, while the app is in the background, the local name is not
     * advertised and service UUIDs are placed in a special "overflow" area).
     *
     * @throws IllegalStateException if the server is not [started][start].
     * @throws AdvertiseException if advertising could not be started (e.g. advertisement data too large).
     */
    public suspend fun advertise(builderAction: AdvertisementParametersBuilder.() -> Unit = {})

    /**
     * Sends a notification (or indication, per the [onSubscription][CharacteristicBuilder.onSubscription]
     * configuration of the characteristic) carrying [value] to [centrals] subscribed to
     * [characteristic]. When [centrals] is `null` (default), all subscribed centrals are notified.
     * Centrals that are not subscribed to [characteristic] are ignored.
     *
     * [characteristic] can be created via [characteristicOf], and must identify a characteristic
     * (declared via the [GattServer] builder DSL) that was configured with
     * [onSubscription][CharacteristicBuilder.onSubscription].
     *
     * Suspends until the notification has been handed off to the platform (e.g. when notifications
     * are being sent faster than the connection can transmit them, backpressure is applied by
     * suspending).
     *
     * @throws IllegalStateException if the server is not [started][start].
     * @throws NoSuchElementException if [characteristic] was not declared via the [GattServer] builder DSL.
     * @throws IllegalArgumentException if [characteristic] was not configured with [onSubscription][CharacteristicBuilder.onSubscription].
     */
    public suspend fun notify(
        characteristic: Characteristic,
        value: ByteArray,
        centrals: Collection<Central>? = null,
    )

    /**
     * Provides a [StateFlow] of the remote [centrals][Central] currently subscribed to
     * [characteristic].
     *
     * @throws NoSuchElementException if [characteristic] was not declared via the [GattServer] builder DSL.
     * @throws IllegalArgumentException if [characteristic] was not configured with [onSubscription][CharacteristicBuilder.onSubscription].
     */
    public fun subscribers(characteristic: Characteristic): StateFlow<Set<Central>>
}

/**
 * Creates a [GattServer] with a GATT profile (services, characteristics and descriptors) declared
 * via the [builderAction] DSL.
 *
 * The returned server is not running; call [GattServer.start] to publish the declared services
 * (and [GattServer.advertise] to advertise to remote centrals).
 */
@ExperimentalKableApi
@Suppress("FunctionName") // Builder function.
public expect fun GattServer(builderAction: GattServerBuilder.() -> Unit): GattServer
