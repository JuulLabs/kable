package com.juul.kable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin

/**
 * A mechanism for launching and awaiting some shared job repeatedly.
 *
 * The job is launched by calling [getOrAsync]. Subsequent calls to [getOrAsync] will return the
 * same (i.e. shared) [job][Deferred] if it is still executing.
 */
internal class SharedRepeatableTask<T>(
    private val scope: CoroutineScope,
    private val task: suspend CoroutineScope.() -> T
) {

    private var deferred: Deferred<T>? = null

    /** If there is a running [job][Deferred] returns it. Otherwise, launches and returns a new [job][Deferred]. */
    fun getOrAsync() = deferred
        ?.takeUnless { it.isCompleted }
        ?: scope.async(block = task).apply { deferred = this }

    /**
     * Cancels the running job (if any) and suspends until it completes (either normally or exceptionally).
     *
     * Throws an [IllegalStateException] if [getOrAsync] has never been called.
     */
    suspend fun cancelAndJoin() {
        checkNotNull(deferred).cancelAndJoin()
    }
}

internal fun CoroutineScope.sharedRepeatableTask(
    task: suspend CoroutineScope.() -> Unit
) = SharedRepeatableTask(this, task)
