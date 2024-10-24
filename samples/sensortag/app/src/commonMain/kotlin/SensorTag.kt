package com.juul.sensortag

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.juul.kable.Bluetooth
import com.juul.kable.ExperimentalApi
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.characteristicOf
import com.juul.kable.logs.Logging.Level.Events
import com.juul.khronicle.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val GYRO_MULTIPLIER = 500f / 65536f

private val movementSensorServiceUuid = sensorTagUuid("aa80")
private val movementSensorDataUuid = sensorTagUuid("aa81")
private val movementNotificationUuid = sensorTagUuid("2902")
private val movementConfigurationUuid = sensorTagUuid("aa82")
private val movementPeriodUuid = sensorTagUuid("aa83")
private val clientCharacteristicConfigUuid = Bluetooth.BaseUuid + 0x2902

private val movementConfigCharacteristic = characteristicOf(
    service = movementSensorServiceUuid,
    characteristic = movementConfigurationUuid,
)

private val movementDataCharacteristic = characteristicOf(
    service = movementSensorServiceUuid,
    characteristic = movementSensorDataUuid,
)

private val movementPeriodCharacteristic = characteristicOf(
    service = movementSensorServiceUuid,
    characteristic = movementPeriodUuid,
)

private val rssiInterval = 5.seconds

class SensorTag(private val peripheral: Peripheral) {

    companion object {
        val Uuid = uuidFrom("0000aa80-0000-1000-8000-00805f9b34fb")
        val PeriodRange = 100.milliseconds..2550.milliseconds

        val services = listOf(
            movementSensorServiceUuid,
            movementSensorDataUuid,
            movementNotificationUuid,
            movementConfigurationUuid,
            movementPeriodUuid,
            clientCharacteristicConfigUuid,
        )

        val scanner by lazy {
            Scanner {
                logging {
                    level = Events
                }
                filters {
                    match {
                        services = listOf(Uuid)
                    }
                }
            }
        }
    }

    val state = peripheral.state

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
        .map { it * GYRO_MULTIPLIER }

    suspend fun connect() {
        Log.info { "Connecting" }
        try {
            peripheral.connect().launch { monitorRssi() }
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
                @OptIn(ExperimentalApi::class)
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
}

private fun sensorTagUuid(short16BitUuid: String): Uuid =
    uuidFrom("f000${short16BitUuid.lowercase()}-0451-4000-b000-000000000000")

private fun characteristicOf(service: Uuid, characteristic: Uuid) =
    characteristicOf(service.toString(), characteristic.toString())
