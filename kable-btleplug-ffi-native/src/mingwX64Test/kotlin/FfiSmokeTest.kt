package com.juul.kable.btleplug.ffi

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class FfiSmokeTest {

    /**
     * Exercises the Rust FFI end-to-end (tokio runtime startup, WinRT interaction and the
     * async-callback machinery across cinterop). The return value is not asserted, as it depends
     * on the Bluetooth hardware of the machine running the test.
     */
    @Test
    fun isSupported_returnsWithoutCrashing() {
        runBlocking {
            withTimeout(30.seconds) {
                println("isSupported: ${isSupported()}")
            }
        }
    }
}
