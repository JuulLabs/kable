package com.juul.kable.server

import com.juul.kable.ExperimentalKableApi
import kotlinx.coroutines.CoroutineScope

/**
 * Action invoked when a remote [Central] reads a characteristic (or descriptor), returning the full
 * value of the attribute. May throw [GattErrorException] to reject the read.
 */
public typealias ReadAction = suspend ReadRequest.() -> ByteArray

/**
 * Action invoked when a remote [Central] writes a characteristic (or descriptor). May throw
 * [GattErrorException] to reject the write.
 */
public typealias WriteAction = suspend WriteRequest.(value: ByteArray) -> Unit

/**
 * Action launched (in a dedicated coroutine, per subscribed [Central]) when a remote [Central]
 * subscribes to a characteristic; cancelled when the [Central] unsubscribes (or disconnects), or
 * the server is [stopped][GattServer.stop].
 */
public typealias SubscriptionAction = suspend SubscriptionScope.() -> Unit

/** Scope of a [ReadAction], providing details of the read request being served. */
@ExperimentalKableApi
public class ReadRequest internal constructor(

    /** Remote [Central] performing the read. */
    public val central: Central,
)

/** Scope of a [WriteAction], providing details of the write request being served. */
@ExperimentalKableApi
public class WriteRequest internal constructor(

    /** Remote [Central] performing the write. */
    public val central: Central,
)

/** Scope of a [SubscriptionAction]. */
@ExperimentalKableApi
public interface SubscriptionScope : CoroutineScope {

    /** Remote [Central] subscribed to the characteristic. */
    public val central: Central

    /**
     * Sends a notification (or indication, per the [onSubscription][CharacteristicBuilder.onSubscription]
     * configuration) carrying [value] to the subscribed [central].
     *
     * Suspends until the notification has been handed off to the platform (e.g. when notifications
     * are being sent faster than the connection can transmit them, backpressure is applied by
     * suspending).
     */
    public suspend fun send(value: ByteArray)
}
