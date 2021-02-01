package com.juul.kable.test

import com.juul.kable.Advertisement
import com.juul.kable.toNSData
import platform.CoreBluetooth.CBAdvertisementDataManufacturerDataKey
import platform.CoreBluetooth.CBPeripheral
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
        val advertisement = fakeAdvertisement(
            ubyteArrayOf(
                0xc3u, 0x05u, // little-endian manufacturer id
                0x042u, // data
            )
        )
        val data = advertisement.manufacturerData
        assertNotNull(data)
        assertEquals(data.code, 0x05c3)
        assertEquals(data.data.size, 1)
        assertEquals(data.data[0], 0x042)
    }

    @Test
    fun manufacturerData_advertisementWithTwoBytes_hasCodeAndEmptyData() {
        val advertisement = fakeAdvertisement(
            ubyteArrayOf(
                0xc3u, 0x05u, // little-endian manufacturer id
            )
        )
        val data = advertisement.manufacturerData
        assertNotNull(data)
        assertEquals(data.code, 0x05c3)
        assertTrue(data.data.isEmpty())
    }

    @Test
    fun manufacturerData_advertisementWithFewerThanTwoBytes_isNull() {
        val advertisement = fakeAdvertisement(
            ubyteArrayOf(0x01u)
        )
        val data = advertisement.manufacturerData
        assertNull(data)
    }

    private fun fakeAdvertisement(manufacturerBytes: UByteArray): Advertisement =
        Advertisement(
            rssi = 0,
            data = mapOf(
                CBAdvertisementDataManufacturerDataKey to manufacturerBytes.toByteArray().toNSData()
            ),
            cbPeripheral = CBPeripheral()
        )
}
