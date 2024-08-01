package com.juul.kable

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private val threadNumber = atomic(0)
private fun generateThreadName() = "Kable#${threadNumber.incrementAndGet()}"

public interface ThreadingStrategy {
    public fun acquire(): Threading
    public fun release(threading: Threading)
}

/**
 * A [ThreadingStrategy] that creates ["threads"][Threading] immediately as needed and immediately
 * shuts down when immediately when [released][release].
 */
public object OnDemandThreadingStrategy : ThreadingStrategy {

    override fun acquire(): Threading = Threading(generateThreadName())

    override fun release(threading: Threading) {
        threading.shutdown()
    }
}

/**
 * A [ThreadingStrategy] that pools unused ["threads"][Threading] until [evictAfter] time has
 * elapsed.
 *
 * You should only create a single [PooledThreadingStrategy] instance per application run, as it
 * holds the "shared" pool of unused ["threads"][Threading].
 *
 * Useful for when [Peripheral] connections are quickly being spun down and up again — as they can
 * [acquire] their ["threads"][Threading] from the unused pool.
 *
 * If [Peripheral] connections are expected to be long running, or for there to be long down times
 * between connections, [OnDemandThreadingStrategy] may be a better choice.
 */
public class PooledThreadingStrategy(
    scope: CoroutineScope = GlobalScope,
    private val evictAfter: Duration = 1.minutes,
) : ThreadingStrategy {

    private val pool = mutableListOf<Pair<TimeMark, Threading>>()
    private val guard = reentrantLock()

    private val job = scope.launch {
        while (true) {
            guard.withLock {
                pool.removeAll { (timeMark) -> timeMark.hasPassedNow() }
            }
            delay(evictAfter / 2)
        }
    }

    public fun cancel(): Unit = job.cancel()

    override fun acquire(): Threading = guard.withLock {
        pool.removeFirstOrNull()
            ?.let { (_, threading) -> threading }
    } ?: Threading(generateThreadName())

    override fun release(threading: Threading) {
        guard.withLock {
            val evictAt = TimeSource.Monotonic.markNow() + evictAfter
            pool.add(evictAt to threading)
        }
    }
}
