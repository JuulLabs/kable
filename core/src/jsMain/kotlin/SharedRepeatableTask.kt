package com.juul.kable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

/**
 * A mechanism for launching and awaiting some shared job repeatedly.
 *
 * The job is launched by calling [launchAsync]. Subsequent calls to [launchAsync] will return the
 * same (i.e. shared) job if it is still executing.
 */
internal class SharedRepeatableTask(
    private val scope: CoroutineScope,
    private val task: suspend CoroutineScope.() -> Unit
) {

    private var job: Job? = null

    /**
     * Launches a shared instance of the job if it is not running.
     * Subsequent calls, while the job is still live, will return the existing job.
     * Once the job is completed, a subsequent call will launch a new job.
     */
    fun launch(): Job =
        job ?: scope.launch(block = task).apply {
            job = this
            invokeOnCompletion { job = null }
        }

    fun cancel() {
        job?.cancel()
    }

    suspend fun cancelAndJoin() {
        job?.cancelAndJoin()
    }

    suspend fun join() {
        job?.join()
    }
}

internal fun CoroutineScope.sharedRepeatableTask(
    task: suspend CoroutineScope.() -> Unit
) = SharedRepeatableTask(this, task)
