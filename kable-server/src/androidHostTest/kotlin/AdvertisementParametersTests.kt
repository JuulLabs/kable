package com.juul.kable.server

import android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_POWER
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM
import android.os.Build
import android.os.ParcelUuid
import com.juul.kable.Bluetooth
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.toJavaUuid

private val serviceUuid = Bluetooth.BaseUuid + 0x180D

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class AdvertisementParametersTests {

    @Test
    fun defaults_matchAndroidDefaults() {
        val parameters = AdvertisementParametersBuilder().build()
        val settings = parameters.toAdvertiseSettings()
        val data = parameters.toAdvertiseData()

        assertTrue(settings.isConnectable)
        assertEquals(0, settings.timeout)
        assertEquals(ADVERTISE_MODE_LOW_POWER, settings.mode)
        assertEquals(ADVERTISE_TX_POWER_MEDIUM, settings.txPowerLevel)
        assertFalse(data.includeDeviceName)
        assertFalse(data.includeTxPowerLevel)
    }

    @Test
    fun settings_areMapped() {
        val settings = AdvertisementParametersBuilder()
            .apply {
                connectable = false
                timeout = 5.seconds
                mode = AdvertiseMode.LowLatency
                txPower = AdvertiseTxPower.High
            }
            .build()
            .toAdvertiseSettings()

        assertFalse(settings.isConnectable)
        assertEquals(5_000, settings.timeout)
        assertEquals(ADVERTISE_MODE_LOW_LATENCY, settings.mode)
        assertEquals(ADVERTISE_TX_POWER_HIGH, settings.txPowerLevel)
    }

    @Test
    fun data_isMapped() {
        val data = AdvertisementParametersBuilder()
            .apply {
                name = "Example"
                services = listOf(serviceUuid)
                includeTxPowerLevel = true
                manufacturerData(0x004C, byteArrayOf(1, 2))
                serviceData(serviceUuid, byteArrayOf(3, 4))
            }
            .build()
            .toAdvertiseData()

        assertTrue(data.includeDeviceName)
        assertTrue(data.includeTxPowerLevel)
        assertEquals(listOf(ParcelUuid(serviceUuid.toJavaUuid())), data.serviceUuids)
        assertContentEquals(byteArrayOf(1, 2), data.manufacturerSpecificData[0x004C])
        assertContentEquals(byteArrayOf(3, 4), data.serviceData[ParcelUuid(serviceUuid.toJavaUuid())])
    }

    @Test
    fun timeout_greaterThanAndroidLimit_throws() {
        assertFailsWith<IllegalArgumentException> {
            AdvertisementParametersBuilder()
                .apply { timeout = 181.seconds }
                .build()
        }
    }
}
