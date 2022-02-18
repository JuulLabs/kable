package com.juul.sensortag

import com.juul.kable.Peripheral
import com.juul.kable.WriteType
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.characteristicOf
import com.juul.tuulbox.encoding.toHexString
import com.juul.tuulbox.logging.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map

private const val MOVEMENT_SENSOR_SERVICE_UUID = "F000AA80-0451-4000-B000-000000000000"

private val movementConfigCharacteristic = characteristicOf(
    service = MOVEMENT_SENSOR_SERVICE_UUID,
    characteristic = "F000AA82-0451-4000-B000-000000000000",
)

private val movementDataCharacteristic = characteristicOf(
    service = MOVEMENT_SENSOR_SERVICE_UUID,
    characteristic = "F000AA81-0451-4000-B000-000000000000",
)

private val movementPeriodCharacteristic = characteristicOf(
    service = MOVEMENT_SENSOR_SERVICE_UUID,
    characteristic = "F000AA83-0451-4000-B000-000000000000",
)

data class Vector3f(val x: Float, val y: Float, val z: Float)

private const val GYRO_MULTIPLIER = 500f / 65536f

class SensorTag(
    private val peripheral: Peripheral
) : Peripheral by peripheral {

    val gyro: Flow<Vector3f> = peripheral.observe(movementDataCharacteristic)
        .map { data ->
            Vector3f(
                x = data.x * GYRO_MULTIPLIER,
                y = data.y * GYRO_MULTIPLIER,
                z = data.z * GYRO_MULTIPLIER,
            )
        }

    /** Set period, allowable range is 100-2550 ms. */
    suspend fun writeGyroPeriod(periodMillis: Long) {
        val value = periodMillis / 10
        val data = byteArrayOf(value.toByte())

        Log.info { "movement → writePeriod → data = $value (${data.toHexString()})" }
        peripheral.write(movementPeriodCharacteristic, data, WithResponse)
        Log.info { "writeGyroPeriod complete" }
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

private inline val ByteArray.x: Short get() = readShort(0)
private inline val ByteArray.y: Short get() = readShort(2)
private inline val ByteArray.z: Short get() = readShort(4)

private inline infix fun Byte.and(other: Int): Int = toInt() and other
private inline fun ByteArray.readShort(offset: Int): Short {
    val value = get(offset) and 0xff or (get(offset + 1) and 0xff shl 8)
    return value.toShort()
}
