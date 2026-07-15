package com.juul.kable

import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class PooledThreadingStrategyTests {

    @Test
    fun acquire_emptyPool_createsNewThreading() {
        val job = Job()
        val strategy = PooledThreadingStrategy(scope = CoroutineScope(job))
        try {
            val first = strategy.acquire()
            val second = strategy.acquire()
            assertNotSame(first, second)
            first.shutdown()
            second.shutdown()
        } finally {
            strategy.cancel()
        }
    }

    @Test
    fun acquire_afterRelease_reusesPooledThreading() {
        val job = Job()
        val strategy = PooledThreadingStrategy(scope = CoroutineScope(job))
        try {
            val threading = strategy.acquire()
            strategy.release(threading)
            val reacquired = strategy.acquire()
            assertSame(threading, reacquired)
            reacquired.shutdown()
        } finally {
            strategy.cancel()
        }
    }
}
