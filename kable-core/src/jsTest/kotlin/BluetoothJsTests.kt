package com.juul.kable

import com.juul.kable.external.Bluetooth
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class BluetoothJsTests {

    @Test
    fun bluetoothOrThrow_browserUnitTest_returnsBluetooth() = runTest {
        if (isBrowser) {
            assertIs<Bluetooth>(bluetoothOrThrow())
        }
    }

    // In Node.js unit tests, bluetooth is unavailable.
    @Test
    fun bluetoothOrThrow_nodeJsUnitTest_throwsIllegalStateException() = runTest {
        if (isNode) {
            assertFailsWith<IllegalStateException> {
                bluetoothOrThrow()
            }
        }
    }
}
