package com.juul.kable

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RequestPeripheralTests {

    @Test
    fun requestPeripheral_unitTest_throwsIllegalStateException() = runTest {
        // In browser unit tests, bluetooth is not allowed per security restrictions.
        // In Node.js unit tests, bluetooth is unavailable.
        assertFailsWith<IllegalStateException> {
            requestPeripheral(Options {}, this)
        }
    }
}
