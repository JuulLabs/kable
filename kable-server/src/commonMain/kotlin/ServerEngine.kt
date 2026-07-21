package com.juul.kable.server

import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Platform specific backend of a [GattServer].
 *
 * A [ServerEngine] is responsible for translating platform callbacks into [InboundRequest]s
 * (processed by the [RequestDispatcher]) and performing platform I/O (publishing services, sending
 * notifications, advertising).
 */
internal interface ServerEngine {

    /**
     * Opens the platform GATT server and publishes the services of [profile], returning a (fresh)
     * channel of [InboundRequest]s for the server session (requests received before the channel is
     * consumed are buffered).
     *
     * @throws IllegalStateException if the platform GATT server could not be opened.
     */
    suspend fun open(profile: ServerProfile): ReceiveChannel<InboundRequest>

    /** Unpublishes services and closes the platform GATT server (undoes [open]). */
    suspend fun close()

    /**
     * Sends a notification (or indication, per the [characteristic]'s [SubscriptionHandler]
     * configuration) carrying [value] to [central], suspending until the notification has been
     * handed off to the platform (applying backpressure when needed).
     */
    suspend fun notify(central: Central, characteristic: ServerCharacteristic, value: ByteArray)

    /**
     * Starts advertising (per [parameters]) and suspends until cancelled (advertising is stopped
     * when the calling coroutine is cancelled).
     *
     * @throws AdvertiseException if advertising could not be started.
     */
    suspend fun advertise(parameters: AdvertisementParameters): Nothing
}
