package com.juul.sensortag

import com.juul.kable.Bluetooth
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.characteristic
import com.juul.kable.characteristicOf
import com.juul.kable.logs.Logging.Level.Events
import com.juul.kable.service
import com.juul.khronicle.Log
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.IOException

const val movementService16bitUuid = 0xAA80
val movementServiceUuid = SensorTag.BaseUuid + movementService16bitUuid

private val movementDataCharacteristic = characteristicOf(
    service = movementServiceUuid,
    characteristic = SensorTag.BaseUuid + 0xAA81,
)
private val movementConfigCharacteristic = characteristicOf(
    service = movementServiceUuid,
    characteristic = SensorTag.BaseUuid + 0xAA82
)
private val movementPeriodCharacteristic = characteristicOf(
    service = movementServiceUuid,
    characteristic = SensorTag.BaseUuid + 0xAA83
)
val batteryCharacteristic = characteristicOf(
    service = Uuid.service("battery_service"),
    characteristic = Uuid.characteristic("battery_level"),
)

private val rssiInterval = 5.seconds

class SensorTag(private val peripheral: Peripheral) {

    /** SensorTag base UUID: f0000000-0451-4000-b000-000000000000 */
    object BaseUuid {

        private const val mostSignificantBits = -1152921504534413312L // f0000000-0451-4000
        private const val leastSignificantBits = -5764607523034234880L // b000-000000000000

        operator fun plus(shortUuid: Int): Uuid = plus(shortUuid.toLong())

        /** @param shortUuid 32-bits (or less) short UUID (if larger than 32-bits, will be truncated to 32-bits). */
        operator fun plus(shortUuid: Long): Uuid =
            Uuid.fromLongs(mostSignificantBits + (shortUuid and 0xFFFF_FFFF shl 32), leastSignificantBits)

        override fun toString(): String = "f0000000-0451-4000-b000-000000000000"
    }

    companion object {
        val AdvertisedServices = listOf(Bluetooth.BaseUuid + movementService16bitUuid)
        const val GyroMultiplier = 500f / 65536f
        val PeriodRange = 100.milliseconds..2550.milliseconds

        val scanner by lazy {
            Scanner {
                logging {
                    level = Events
                }
                filters {
                    match { services = AdvertisedServices }
                }
            }
        }
    }

    val state = peripheral.state

    private val _battery = MutableStateFlow<ByteArray?>(null)

    /** Battery percent level (0-100). */
    val battery = merge(
        _battery.filterNotNull(),
        peripheral.observe(batteryCharacteristic),
    ).map(ByteArray::first)
        .map(Byte::toInt)

    private val _rssi = MutableStateFlow<Int?>(null)
    val rssi = _rssi.asStateFlow()

    private val _periodMillis = MutableStateFlow<Duration?>(null)
    val periodMillis = _periodMillis.filterNotNull()

    suspend fun setPeriod(period: Duration) {
        writeGyroPeriod(period)
        _periodMillis.value = period
    }

    val gyro: Flow<Vector3f> = peripheral
        .observe(movementDataCharacteristic)
        .map(::Vector3f)
        .map { it * GyroMultiplier }

    suspend fun connect() {
        Log.info { "Connecting" }
        try {
            peripheral.connect().launch { monitorRssi() }
            _battery.value = readBatteryLevel()
            _periodMillis.value = readGyroPeriod()
            enableGyro()
            Log.info { "Connected" }
        } catch (e: IOException) {
            Log.warn(e) { "Connection attempt failed" }
            peripheral.disconnect()
        }
    }

    suspend fun disconnect() {
        peripheral.disconnect()
    }

    private suspend fun monitorRssi() {
        try {
            while (coroutineContext.isActive) {
                _rssi.value = peripheral.rssi()

                Log.debug { "RSSI: ${_rssi.value}" }
                delay(rssiInterval)
            }
        } catch (e: UnsupportedOperationException) {
            // As of Chrome 128, RSSI is not yet supported (even with
            // `chrome://flags/#enable-experimental-web-platform-features` flag enabled).
            Log.warn(e) { "RSSI is not supported" }
        }
    }

    /** Set period, allowable range is 100-2550 ms. */
    private suspend fun writeGyroPeriod(period: Duration) {
        require(period in PeriodRange) { "Period must be in the range $PeriodRange, was $period." }

        val value = period.inWholeMilliseconds / 10
        val data = byteArrayOf(value.toByte())

        Log.verbose { "Writing gyro period of $period" }
        peripheral.write(movementPeriodCharacteristic, data, WithResponse)
        Log.info { "Writing gyro period complete" }
    }

    /** Period within the range 100-2550 ms. */
    private suspend fun readGyroPeriod(): Duration {
        val value = peripheral.read(movementPeriodCharacteristic)
        return ((value[0].toInt() and 0xFF) * 10).milliseconds
    }

    private suspend fun enableGyro() {
        Log.info { "Enabling gyro" }
        peripheral.write(movementConfigCharacteristic, byteArrayOf(0x7F, 0x0), WithResponse)
        Log.info { "Gyro enabled" }
    }

    private suspend fun disableGyro() {
        peripheral.write(movementConfigCharacteristic, byteArrayOf(0x0, 0x0), WithResponse)
    }

    private suspend fun readBatteryLevel(): ByteArray =
        peripheral.read(batteryCharacteristic)
}
