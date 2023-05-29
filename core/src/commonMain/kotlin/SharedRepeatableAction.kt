package com.juul.kable

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext

/**
 * A mechanism for launching and awaiting a shared action ([Deferred]) repeatedly.
 *
 * The [action] is started by calling [getOrAsync]. Subsequent calls to [getOrAsync] will return the
 * same (i.e. shared) [action] until cancelled or failure occurs.
 *
 * The [action] is passed a [scope][CoroutineScope] that can be used to spawn coroutines that can
 * outlive the [action]. [getOrAsync] will continue to return the same action until either
 * [cancelled][cancel], or a failures occurs in either the [action] or any coroutines spawned from
 * the [scope][CoroutineScope] provided to [action].
 *
 * An exception thrown from [action] will cancel any coroutines spawned from the
 * [scope][CoroutineScope] that was provided to the [action].
 *
 * Calling [cancel] or [cancelAndJoin] will cancel the [action] and any coroutines created from the
 * [scope][CoroutineScope] provided to the [action]. A subsequent call to [getOrAsync] will then
 * start the [action] again.
 */
internal class SharedRepeatableAction<T>(
    private val coroutineContext: CoroutineContext,
    private val action: suspend (scope: CoroutineScope) -> T,
) {

    private class Jobs<T>(
        val root: Job,
        val action: Deferred<T>,
        var isCancelled: Boolean = false,
    )

    private var jobs: Jobs<T>? = null
    private val guard = reentrantLock()

    fun getOrAsync(): Deferred<T> = guard.withLock {
        // ktlint-disable indent
        (
            jobs?.takeUnless { it.isCancelled } ?: run {
                val rootJob = Job(coroutineContext.job)
                val rootScope = CoroutineScope(coroutineContext + rootJob)
                val actionJob = rootScope.async { action(rootScope) }.apply {
                    invokeOnCompletion { cause ->
                        if (cause != null) this@SharedRepeatableAction.cancel()
                    }
                }
                Jobs(rootJob, actionJob)
            }.also { jobs = it }
        ).action
        // ktlint-enable indent
    }

    fun cancel() {
        guard.withLock {
            jobs?.apply { isCancelled = true }
        }?.root?.cancel()
    }

    suspend fun cancelAndJoin() {
        guard.withLock {
            jobs?.apply { isCancelled = true }
        }?.root?.cancelAndJoin()
    }
}

internal fun <T> CoroutineScope.sharedRepeatableAction(
    action: suspend (scope: CoroutineScope) -> T,
) = SharedRepeatableAction(coroutineContext, action)
