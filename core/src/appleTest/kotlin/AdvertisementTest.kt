package com.juul.kable.test

import com.juul.kable.toManufacturerData
import com.juul.kable.toNSData
import platform.Foundation.NSData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Test function naming format:
// [name of item being tested]_[input conditions]_[expected results]
class AdvertisementTest {
    @Test
    fun manufacturerData_advertisementWithMoreThanTwoBytes_hasCodeAndData() {
        val data = ubyteArrayOf(
            0xc3u, 0x05u, // little-endian manufacturer id
            0x042u, // data
        ).toNSData()
        val manufacturerData = data.toManufacturerData()

        assertNotNull(manufacturerData)
        assertEquals(manufacturerData.code, 0x05c3)
        assertEquals(manufacturerData.data.size, 1)
        assertEquals(manufacturerData.data[0], 0x042)
    }

    @Test
    fun manufacturerData_advertisementWithTwoBytes_hasCodeAndEmptyData() {
        val data = ubyteArrayOf(
            0xc3u, 0x05u, // little-endian manufacturer id
        ).toNSData()
        val manufacturerData = data.toManufacturerData()

        assertNotNull(manufacturerData)
        assertEquals(manufacturerData.code, 0x05c3)
        assertTrue(manufacturerData.data.isEmpty())
    }

    @Test
    fun manufacturerData_advertisementWithFewerThanTwoBytes_isNull() {
        val data = ubyteArrayOf(0x01u).toNSData()
        val manufacturerData = data.toManufacturerData()

        assertNull(manufacturerData)
    }
}

private fun UByteArray.toNSData(): NSData = toByteArray().toNSData()
