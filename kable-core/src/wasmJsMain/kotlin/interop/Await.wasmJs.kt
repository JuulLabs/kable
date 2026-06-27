package com.juul.kable.interop

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.Promise

internal actual suspend fun <T : JsAny?> Promise<T>.await(): T =
    suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
        then(
            onFulfilled = { cont.resume(it); null },
            onRejected = { cont.resumeWithException(it.toThrowable()); null },
        )
    }

private fun JsPromiseError.toThrowable(): Throwable = try {
    unsafeCast<JsReference<Throwable>>().get()
} catch (_: Throwable) {
    asJsException()
}
