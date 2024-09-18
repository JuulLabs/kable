package com.juul.kable

import com.juul.kable.SharedRepeatableAction.State
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal fun <T> CoroutineScope.sharedRepeatableAction(
    action: suspend (scope: CoroutineScope) -> T,
) = SharedRepeatableAction(this, action)

/**
 * A mechanism for launching and awaiting a shared [action][State.action] ([Deferred]) repeatedly.
 *
 * The [action][State.action] is created as a child of [root][State.root] [Job] and encapsulated via
 * a [State]:
 *
 * ```
 *           .-------.
 *           | scope |
 *           '-------'
 *               |
 *             child
 *               |
 *  .------------|------------.
 *  | State      v            |
 *  |     .------------.      |     .----------------.
 *  |     | root (Job) | •••••••••• | CoroutineScope |
 *  |     '------------'      |     '________________'
 *  |            |            |              :
 *  |          child          |          passed as
 *  |            |            |         argument to
 *  |            v            |              :
 *  |  .-------------------.  |              v
 *  |  | action (Deferred) | ••••• `action(scope)`
 *  |  '-------------------'  |
 *  '-------------------------'
 * ```
 *
 * The [action] is started by calling [await]. Subsequent calls to [await] will return
 * the same (i.e. shared) [action] until failure occurs.
 *
 * The [action] is passed a [scope][CoroutineScope] (of the [root][State.root]) that can be used to
 * spawn coroutines that can outlive the successful completion of [action]. [await] will continue to
 * return the same action until a failure occurs in either the [action] or any coroutines spawned
 * from the [scope][CoroutineScope] provided to [action].
 *
 * An exception thrown from [action] will cancel any coroutines spawned from the
 * [scope][CoroutineScope] that was provided to the [action].
 *
 * Calling [cancelAndJoin] will cancel the [root][State.root], the [action] and any coroutines
 * created from the [scope][CoroutineScope] provided to the [action]. A subsequent call to [await]
 * will then start the [action] again.
 */
internal class SharedRepeatableAction<T>(
    private val scope: CoroutineScope,
    private val action: suspend (scope: CoroutineScope) -> T,
) {

    private class State<T>(
        val root: Job,
        val action: Deferred<T>,
    )

    private var state: State<T>? = null
    private val guard = Mutex()

    private suspend fun getOrCreate(): State<T> = guard.withLock {
        state
            ?.takeUnless { it.root.isCompleted }
            ?: create().also { state = it }
    }

    private fun create(): State<T> {
        check(scope.coroutineContext.job.isActive) { "Scope is not active" }
        val root = Job(scope.coroutineContext.job)
        val scope = CoroutineScope(scope.coroutineContext + root)
        val action = scope.async { action(scope) }
        return State(root, action)
    }

    private suspend fun stateOrNull(): State<T>? = guard.withLock { state }

    suspend fun await() = getOrCreate().action.await()

    /**
     * [Cancels][Job.cancelAndJoin] the [root][State.root] (and [action][State.action]) if available.
     */
    suspend fun cancelAndJoin(cause: CancellationException?) {
        stateOrNull()?.root?.cancelAndJoin(cause)
    }
}

private suspend fun Job.cancelAndJoin(cause: CancellationException?) {
    cancel(cause)
    join()
}
