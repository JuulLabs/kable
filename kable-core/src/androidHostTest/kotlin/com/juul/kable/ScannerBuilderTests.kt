package com.juul.kable

import android.os.Build
import kotlinx.coroutines.channels.Channel
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class ScannerBuilderTests {

    @Test
    fun bufferCapacity_default_isUnlimited() {
        assertEquals(Channel.UNLIMITED, ScannerBuilder().bufferCapacity)
    }

    @Test
    fun bufferCapacity_positive_isRetained() {
        val builder = ScannerBuilder()
        builder.bufferCapacity = 64
        assertEquals(64, builder.bufferCapacity)
    }

    @Test
    fun bufferCapacity_buffered_isRejected() {
        // `Channel.BUFFERED` behaves as a capacity of 1 (not the default channel capacity) when
        // combined with an overflow strategy, so it is rejected rather than silently misleading.
        assertFailsWith<IllegalArgumentException> {
            ScannerBuilder().bufferCapacity = Channel.BUFFERED
        }
    }

    @Test
    fun bufferCapacity_conflated_isRejected() {
        assertFailsWith<IllegalArgumentException> {
            ScannerBuilder().bufferCapacity = Channel.CONFLATED
        }
    }

    @Test
    fun bufferCapacity_rendezvous_isRejected() {
        assertFailsWith<IllegalArgumentException> {
            ScannerBuilder().bufferCapacity = 0
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun preConflate_true_conflatesBufferCapacity() {
        val builder = ScannerBuilder()
        builder.preConflate = true
        assertEquals(1, builder.bufferCapacity)
        assertTrue(builder.preConflate)
    }

    @Suppress("DEPRECATION")
    @Test
    fun preConflate_false_isUnlimitedBufferCapacity() {
        val builder = ScannerBuilder()
        builder.preConflate = true
        builder.preConflate = false
        assertEquals(Channel.UNLIMITED, builder.bufferCapacity)
        assertFalse(builder.preConflate)
    }
}
