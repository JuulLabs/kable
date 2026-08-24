package com.juul.kable.btleplug.ffi

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.concurrent.AtomicInt
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

    /**
     * Starts a scan, streams advertisement callbacks for a few seconds, then cancels. Asserts
     * only that the round-trip does not crash or hang; the number of advertisements received
     * depends on the machine's Bluetooth hardware and surroundings. Skips (without failing) on
     * machines without a usable Bluetooth adapter (e.g. CI runners).
     */
    @Test
    fun scan_streamsWithoutCrashing() {
        runBlocking {
            withTimeout(30.seconds) {
                if (!isSupported()) {
                    println("Bluetooth unavailable, skipping scan smoke test")
                    return@withTimeout
                }

                val updates = AtomicInt(0)
                val callback = object : ScanCallback {
                    override suspend fun update(peripheral: PeripheralProperties) {
                        updates.incrementAndGet()
                    }
                }

                val handle = try {
                    scan(callback)
                } catch (exception: Exception) {
                    // A machine can report Bluetooth as supported while its adapter is unable to
                    // scan (e.g. radio powered off).
                    println("Unable to scan (${exception.message}), skipping scan smoke test")
                    return@withTimeout
                }

                try {
                    delay(5.seconds)
                } finally {
                    handle.cancel()
                    handle.destroy()
                }
                println("Advertisement callbacks received: ${updates.value}")
            }
        }
    }
}
