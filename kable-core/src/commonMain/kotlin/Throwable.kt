package com.juul.kable

import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

/**
 * Unwraps the [cause][Throwable.cause] of a [CancellationException].
 *
 * Traverses the [cause][Throwable.cause] until it finds an exception that originated from Kable and
 * returns that [Exception]. If a Kable exception cannot be found, then the receiver [Throwable] is
 * returned.
 *
 * Useful when wanting to convert a coroutine cancellation into a failure, for example:
 * If a failure occurs in a sibling coroutine to that of an async connection process
 * (e.g. `connect()` function), the connection process coroutine will cancel via a
 * [CancellationException] being thrown. The [CancellationException] can be
 * [unwrapped][unwrapCancellationCause] to propagate (from the `connect()` function) the sibling
 * failure rather than [cancellation][CancellationException].
 */
internal fun Throwable.unwrapCancellationCause(): Throwable {
    var exception: Throwable = this
    while (exception is CancellationException) {
        if (exception == exception.cause) return this
        exception = exception.cause ?: return this
        if (exception is IOException || exception is BluetoothException) return exception
    }
    return this
}
