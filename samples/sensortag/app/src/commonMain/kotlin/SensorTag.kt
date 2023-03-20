package com.juul.sensortag

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.juul.kable.Bluetooth
import com.juul.kable.Filter
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.characteristicOf
import com.juul.kable.logs.Logging.Level.Events
import com.juul.tuulbox.encoding.toHexString
import com.juul.tuulbox.logging.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val GYRO_MULTIPLIER = 500f / 65536f

const val sensorTagUuid = "0000aa80-0000-1000-8000-00805f9b34fb"
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

val scanner = Scanner {
    logging {
        level = Events
    }
    filters = listOf(Filter.Service(uuidFrom(sensorTagUuid)))
}

val services = listOf(
    movementSensorServiceUuid,
    movementSensorDataUuid,
    movementNotificationUuid,
    movementConfigurationUuid,
    movementPeriodUuid,
    clientCharacteristicConfigUuid,
)

class SensorTag(
    private val peripheral: Peripheral
) : Peripheral by peripheral {

    val gyro: Flow<Vector3f> = peripheral
        .observe(movementDataCharacteristic)
        .map(::Vector3f)
        .map { it * GYRO_MULTIPLIER }

    /** Set period, allowable range is 100-2550 ms. */
    suspend fun writeGyroPeriod(periodMillis: Long) {
        val value = periodMillis / 10
        val data = byteArrayOf(value.toByte())

        Log.verbose { "Writing gyro period" }
        peripheral.write(movementPeriodCharacteristic, data, WithResponse)
        Log.info { "Writing gyro period complete" }
    }

    /** Period (in milliseconds) within the range 100-2550 ms. */
    suspend fun readGyroPeriod(): Int {
        val value = peripheral.read(movementPeriodCharacteristic)
        Log.info { "movement → readPeriod → value = ${value.toHexString()}" }
        return value[0] * 10
    }

    suspend fun enableGyro() {
        Log.info { "Enabling gyro" }
        peripheral.write(movementConfigCharacteristic, byteArrayOf(0x7F, 0x0), WithResponse)
        Log.info { "Gyro enabled" }
    }

    suspend fun disableGyro() {
        peripheral.write(movementConfigCharacteristic, byteArrayOf(0x0, 0x0), WithResponse)
    }
}

private fun sensorTagUuid(short16BitUuid: String): Uuid =
    uuidFrom("f000${short16BitUuid.lowercase()}-0451-4000-b000-000000000000")

private fun characteristicOf(service: Uuid, characteristic: Uuid) =
    characteristicOf(service.toString(), characteristic.toString())
