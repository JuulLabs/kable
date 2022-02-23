package com.juul.kable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineStart.DEFAULT
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

/**
 * A mechanism for launching and awaiting some shared job repeatedly.
 *
 * The job is launched by calling [launchAsync]. Subsequent calls to [launchAsync] will return the
 * same (i.e. shared) job if it is still executing.
 */
internal class SharedRepeatableTask(
    private val scope: CoroutineScope,
    private val job: suspend () -> Unit
) {

    private var deferred: Deferred<Unit>? = null

    /**
     * Launches a shared instance of the job if it is not running.
     * Subsequent calls, while the job is still live, will return the existing job.
     * Once the job is completed, a subsequent call will launch a new job.
     */
    internal fun launchAsync(start: CoroutineStart = DEFAULT): Deferred<Unit> =
        deferred ?: scope.async(start = start) {
            job.invoke()
        }.apply {
            deferred = this
            invokeOnCompletion { deferred = null }
        }

    internal fun cancel() {
        deferred?.cancel()
    }

    internal suspend fun join() {
        deferred?.join()
    }
}
