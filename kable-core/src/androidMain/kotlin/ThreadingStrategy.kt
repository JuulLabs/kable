package com.juul.kable

import com.juul.kable.OnDemandThreadingStrategy.acquire
import com.juul.kable.OnDemandThreadingStrategy.release
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
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
 * A [ThreadingStrategy] that creates ["threads"][Threading] when [acquired][acquire] and
 * immediately shuts down when [released][release].
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
 * In most circumstances, only a a single [PooledThreadingStrategy] instance should be created per
 * application run, as it holds the "shared" pool of unused ["threads"][Threading].
 *
 * Useful for when [Peripheral] connections are quickly being spun down and up again — as they can
 * re-use existing ["threads"][Threading] ([acquire] their ["threads"][Threading] from the unused
 * pool).
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
        try {
            while (true) {
                guard.withLock {
                    pool.removeAll { (timeMark) -> timeMark.hasPassedNow() }
                }
                delay(evictAfter / 2)
            }
        } catch (e: CancellationException) {
            guard.withLock {
                check(pool.isEmpty()) {
                    "PooledThreadStrategy must complete with an empty pool, but had ${pool.count()} threads in pool"
                }
            }
            throw CancellationException(e)
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
