package com.juul.kable

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * If the exception contains cause that differs from [CancellationException] returns it otherwise
 * returns itself.
 *
 * Useful when wanting to convert a coroutine cancellation into a failure, for example:
 * If a failure occurs in a sibling coroutine to that of an async connection process
 * (e.g. `connect()` function), the connection process coroutine will cancel via a
 * [CancellationException] being thrown. The [CancellationException] can be
 * [unwrapped][unwrapCancellationException] to propagate (from the `connect()` function) the sibling
 * failure rather than [cancellation][CancellationException].
 *
 * Copied from: https://github.com/ktorio/ktor/blob/bcd9de62518add3322dc0aa6d19235c551aaf315/ktor-client/ktor-client-core/jvm/src/io/ktor/client/utils/ExceptionUtilsJvm.kt
 */
internal fun Throwable.unwrapCancellationException(): Throwable {
    var exception: Throwable? = this
    while (exception is CancellationException) {
        // If there is a cycle, we return the initial exception.
        if (exception == exception.cause) return this
        exception = exception.cause
    }
    return exception ?: this
}

internal suspend fun <T> unwrapCancellationExceptions(action: suspend () -> T): T = try {
    action()
} catch (e: CancellationException) {
    currentCoroutineContext().ensureActive()
    throw e.unwrapCancellationException()
}
