package com.juul.kable

import kotlin.test.Test
import kotlin.test.assertEquals

class BluetoothTests {

    @Test
    fun baseUuid_plusInt() {
        assertEquals(
            expected = "00002909-0000-1000-8000-00805F9B34FB",
            actual = (Bluetooth.BaseUuid + 0x2909).toString().uppercase(),
        )
    }

    @Test
    fun baseUuid_plusIntOfFFFF() {
        assertEquals(
            expected = "0000FFFF-0000-1000-8000-00805F9B34FB",
            actual = (Bluetooth.BaseUuid + 0xFFFF).toString().uppercase(),
        )
    }

    @Test
    fun baseUuid_plusMaxInt() {
        assertEquals(
            expected = "7FFFFFFF-0000-1000-8000-00805F9B34FB",
            actual = (Bluetooth.BaseUuid + Int.MAX_VALUE).toString().uppercase(),
        )
    }

    @Test
    fun baseUuid_plusLongOfFFFFFFFF() {
        assertEquals(
            expected = "FFFFFFFF-0000-1000-8000-00805F9B34FB",
            actual = (Bluetooth.BaseUuid + 0xFFFF_FFFF).toString().uppercase(),
        )
    }

    @Test
    fun baseUuid_greaterThan32bits_truncates() {
        assertEquals(
            expected = "FFFFFFFF-0000-1000-8000-00805F9B34FB",
            actual = (Bluetooth.BaseUuid + 0x7FFFF_FFFF_FFFF_FFF).toString().uppercase(),
        )
    }
}
