package com.juul.kable

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext

/**
 * A mechanism for launching and awaiting a shared action ([Deferred]) repeatedly.
 *
 * The [action] is started by calling [await]. Subsequent calls to [await] will return the same
 * (i.e. shared) [action] until failure occurs.
 *
 * The [action] is passed a [scope][CoroutineScope] that can be used to spawn coroutines that can
 * outlive the [action]. [await] will continue to return the same action until a failures occurs in
 * either the [action] or any coroutines spawned from the [scope][CoroutineScope] provided to
 * [action].
 *
 * An exception thrown from [action] will cancel any coroutines spawned from the
 * [scope][CoroutineScope] that was provided to the [action].
 *
 * Calling [cancel] or [cancelAndJoin] will cancel the [action] and any coroutines created from the
 * [scope][CoroutineScope] provided to the [action]. A subsequent call to [await] will then start
 * the [action] again.
 */
internal class SharedRepeatableAction<T>(
    private val coroutineContext: CoroutineContext,
    private val action: suspend (scope: CoroutineScope) -> T,
) {

    private class State<T>(
        val root: Job,
        val action: Deferred<T>,
    )

    private var state: State<T>? = null
    private val guard = reentrantLock()

    suspend fun await() = getOrAsync().await()

    @Suppress("ktlint:standard:indent")
    private fun getOrAsync(): Deferred<T> = guard.withLock {
        (
            state?.takeIf { it.root.isActive } ?: run {
                val rootJob = Job(coroutineContext.job)
                // No-op exception handler prevents any child failures being considered unhandled
                // (which on Android crashes the app) while propagating cancellation to parent and
                // honoring parent cancellation.
                val rootScope = CoroutineScope(coroutineContext + rootJob + noopExceptionHandler)
                val actionDeferred = rootScope.async {
                    action(rootScope)
                }
                State(rootJob, actionDeferred)
            }.also { state = it }
        ).action
    }

    fun cancel(cause: CancellationException? = null) {
        guard.withLock { state }
            ?.root
            ?.cancel(cause)
    }

    suspend fun cancelAndJoin(cause: CancellationException? = null) {
        guard.withLock { state }
            ?.root
            ?.cancelAndJoin(cause)
    }

    suspend fun join() {
        guard.withLock { state }
            ?.root
            ?.join()
    }
}

internal fun <T> CoroutineScope.sharedRepeatableAction(
    action: suspend (scope: CoroutineScope) -> T,
) = SharedRepeatableAction(coroutineContext, action)

private suspend fun Job.cancelAndJoin(cause: CancellationException? = null) {
    cancel(cause)
    join()
}

private val noopExceptionHandler = CoroutineExceptionHandler { _, _ ->
    // No-op
}
