package com.juul.kable.btleplug.ffi

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class FfiSmokeTest {

    @Test
    fun peripheralId_roundTripsAcrossFfi() {
        val address = "01:23:45:67:89:10"
        val id = PeripheralId(address)

        assertEquals(address, id.toString())
        assertEquals(id, PeripheralId(address))
    }

    /**
     * Exercises the async machinery across cinterop (tokio runtime startup and WinRT interaction).
     * Whether an adapter is present depends on the machine running the test, but one that is on
     * must always be reported as supported.
     */
    @Test
    fun isAdapterOn_impliesIsSupported() {
        runBlocking {
            withTimeout(30.seconds) {
                if (isAdapterOn()) assertTrue(isSupported())
            }
        }
    }
}
