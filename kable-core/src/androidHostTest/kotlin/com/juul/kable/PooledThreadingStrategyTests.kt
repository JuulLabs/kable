package com.juul.kable

import android.os.Build
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.M])
class PooledThreadingStrategyTests {

    @Test
    fun acquire_emptyPool_createsNewThreading() = runTest {
        val strategy = PooledThreadingStrategy(scope = backgroundScope)
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
    fun acquire_afterRelease_reusesPooledThreading() = runTest {
        val strategy = PooledThreadingStrategy(scope = backgroundScope)
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
