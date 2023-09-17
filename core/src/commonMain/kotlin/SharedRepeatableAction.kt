package com.juul.kable

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext

/**
 * A mechanism for launching and awaiting a shared action ([Deferred]) repeatedly.
 *
 * The [action] is started by calling [await]. Subsequent calls to [await] will return the same
 * (i.e. shared) [action] until cancelled or failure occurs.
 *
 * The [action] is passed a [scope][CoroutineScope] that can be used to spawn coroutines that can
 * outlive the [action]. [await] will continue to return the same action until either
 * [cancelled][reset], or a failure occurs in either the [action] or any coroutines spawned from the
 * [scope][CoroutineScope] provided to [action].
 *
 * An exception thrown from [action] will cancel any coroutines spawned from the
 * [scope][CoroutineScope] that was provided to the [action].
 *
 * Calling [reset] or [resetAndJoin] will cancel the [action] and any coroutines created from the
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
    ) {
        var isCancelled: Boolean = false
    }

    private var state: State<T>? = null
    private val guard = reentrantLock()

    suspend fun await() = getOrAsync().await()

    @Suppress("ktlint:standard:indent")
    private fun getOrAsync(): Deferred<T> = guard.withLock {
        (
            /* Job hierarchy:
             *
             *               rootJob
             *                  |
             *              actionJob
             *              /   |   \
             * actionDeferred   |    \
             *                 Job   Job..  <-- (Jobs launched from action)
             *
             * - rootJob is a SupervisorJob to prevent propagation of failures through to
             *   SharedRepeatableAction's parent scope
             * - actionJob is a standard Job to propagate failures to all coroutines launched from
             *   `action` lambda
             */
            state?.takeUnless { it.isCancelled } ?: run {
                val rootJob = SupervisorJob(coroutineContext.job)
                val actionJob = Job(rootJob)
                val actionScope = CoroutineScope(coroutineContext + actionJob)
                val actionDeferred = actionScope.async { action(actionScope) }.apply {
                    invokeOnCompletion { cause ->
                        if (cause != null) {
                            this@SharedRepeatableAction.reset(CancellationException(cause.message, cause))
                        }
                    }
                }
                State(rootJob, actionDeferred)
            }.also { state = it }
        ).action
    }

    fun reset() {
        reset(cause = null)
    }

    private fun reset(cause: CancellationException?) {
        guard.withLock {
            state?.apply { isCancelled = true }
        }?.root?.cancel(cause)
    }

    suspend fun resetAndJoin() {
        guard.withLock {
            state?.apply { isCancelled = true }
        }?.root?.cancelAndJoin()
    }
}

internal fun <T> CoroutineScope.sharedRepeatableAction(
    action: suspend (scope: CoroutineScope) -> T,
) = SharedRepeatableAction(coroutineContext, action)
