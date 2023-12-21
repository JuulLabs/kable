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

public object OnDemandThreadingStrategy : ThreadingStrategy {

    override fun acquire(): Threading = Threading(generateThreadName())

    override fun release(threading: Threading) {
        threading.shutdown()
    }
}

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
        pool.removeFirstOrNull()?.second
    } ?: Threading(generateThreadName())

    override fun release(threading: Threading) {
        guard.withLock {
            val evictAt = TimeSource.Monotonic.markNow() + evictAfter
            pool.add(evictAt to threading)
        }
    }
}
