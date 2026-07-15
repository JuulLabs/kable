package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.os.Build
import android.os.Parcel
import android.os.ParcelUuid
import android.os.Parcelable
import android.util.SparseArray
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

private const val MANUFACTURER_CODE = 0x004C

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class AdvertisementSerializationTests {

    private val serviceUuid = Uuid.parse("0000180a-0000-1000-8000-00805f9b34fb")

    private fun scanResultAdvertisement(): PlatformAdvertisement {
        val bluetoothDevice = mockk<BluetoothDevice> {
            every { address } returns "00:11:22:AA:BB:CC"
            every { name } returns "Example (cached)"
        }
        val record = mockk<ScanRecord> {
            every { deviceName } returns "Example"
            every { bytes } returns byteArrayOf(0x0F, 0x09)
            every { txPowerLevel } returns 8
            every { serviceUuids } returns listOf(ParcelUuid(serviceUuid.toJavaUuid()))
            every { serviceData } returns
                mapOf(ParcelUuid(serviceUuid.toJavaUuid()) to byteArrayOf(0x01, 0x02, 0x03))
            every { getManufacturerSpecificData(MANUFACTURER_CODE) } returns byteArrayOf(0x02, 0x15)
            every { manufacturerSpecificData } returns
                SparseArray<ByteArray>().apply { put(MANUFACTURER_CODE, byteArrayOf(0x02, 0x15)) }
        }
        val scanResult = mockk<ScanResult> {
            every { device } returns bluetoothDevice
            every { rssi } returns -42
            every { isConnectable } returns true
            every { scanRecord } returns record
        }
        return ScanResultAndroidAdvertisement(scanResult)
    }

    @Test
    fun jsonRoundTrip_retainsAdvertisementData() {
        val advertisement = scanResultAdvertisement()

        val json = Json.encodeToString(PlatformAdvertisementSerializer, advertisement)
        val restored = Json.decodeFromString(PlatformAdvertisementSerializer, json)

        assertEquals(advertisement.name, restored.name)
        assertEquals(advertisement.peripheralName, restored.peripheralName)
        assertEquals(advertisement.address, restored.address)
        assertEquals(advertisement.identifier, restored.identifier)
        assertEquals(advertisement.isConnectable, restored.isConnectable)
        assertEquals(advertisement.rssi, restored.rssi)
        assertEquals(advertisement.txPower, restored.txPower)
        assertEquals(advertisement.uuids, restored.uuids)
        assertContentEquals(advertisement.bytes, restored.bytes)
        assertContentEquals(advertisement.serviceData(serviceUuid), restored.serviceData(serviceUuid))
        assertContentEquals(
            advertisement.manufacturerData(MANUFACTURER_CODE),
            restored.manufacturerData(MANUFACTURER_CODE),
        )
        assertEquals(advertisement.manufacturerData?.code, restored.manufacturerData?.code)
        assertContentEquals(advertisement.manufacturerData?.data, restored.manufacturerData?.data)
    }

    @Test
    fun jsonRoundTrip_ofRestoredAdvertisement_isEqual() {
        val advertisement = scanResultAdvertisement()
        val json = Json.encodeToString(PlatformAdvertisementSerializer, advertisement)
        val restored = Json.decodeFromString(PlatformAdvertisementSerializer, json)

        val jsonOfRestored = Json.encodeToString(PlatformAdvertisementSerializer, restored)
        val restoredAgain = Json.decodeFromString(PlatformAdvertisementSerializer, jsonOfRestored)

        assertEquals(json, jsonOfRestored)
        assertEquals(restored, restoredAgain)
    }

    @Test
    fun serializesViaAdvertisementSerializer_matchesPlatformAdvertisementSerializer() {
        val advertisement = scanResultAdvertisement()

        val json = Json.encodeToString(AdvertisementSerializer, advertisement)

        assertEquals(Json.encodeToString(PlatformAdvertisementSerializer, advertisement), json)
    }

    @Test
    fun parcelRoundTrip_ofRestoredAdvertisement_isEqual() {
        val advertisement = scanResultAdvertisement()
        val json = Json.encodeToString(PlatformAdvertisementSerializer, advertisement)
        val restored = Json.decodeFromString(PlatformAdvertisementSerializer, json)
            as CapturedAndroidAdvertisement

        val parcel = Parcel.obtain()
        try {
            restored.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)
            @Suppress("UNCHECKED_CAST")
            val creator = CapturedAndroidAdvertisement::class.java
                .getField("CREATOR")
                .get(null) as Parcelable.Creator<CapturedAndroidAdvertisement>
            val fromParcel = creator.createFromParcel(parcel)

            assertEquals(restored, fromParcel)
        } finally {
            parcel.recycle()
        }
    }
}
