package com.juul.kable

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.uuid.Uuid

class AdvertisementCaptureTests {

    private val serviceUuid = Uuid.parse("0000180a-0000-1000-8000-00805f9b34fb")

    private fun capture(rssi: Int = -42) = AdvertisementCapture(
        name = "Example",
        peripheralName = "Example (cached)",
        identifier = "00:11:22:AA:BB:CC",
        isConnectable = true,
        rssi = rssi,
        txPower = 8,
        uuids = listOf(serviceUuid),
        serviceData = mapOf(serviceUuid to byteArrayOf(0x01, 0x02, 0x03)),
        manufacturerData = mapOf(0x004C to byteArrayOf(0x02, 0x15)),
        bytes = byteArrayOf(0x0F, 0x09),
    )

    @Test
    fun jsonRoundTrip_isEqualToOriginal() {
        val capture = capture()
        val json = Json.encodeToString(AdvertisementCapture.serializer(), capture)
        val decoded = Json.decodeFromString(AdvertisementCapture.serializer(), json)
        assertEquals(capture, decoded)
        assertEquals(capture.hashCode(), decoded.hashCode())
    }

    @Test
    fun equals_capturesWithDifferentData_areNotEqual() {
        assertNotEquals(capture(rssi = -42), capture(rssi = -43))
    }
}
