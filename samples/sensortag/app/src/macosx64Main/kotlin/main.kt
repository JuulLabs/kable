package com.juul.sensortag

import com.juul.kable.State
import com.juul.kable.central
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    val central = central()
    Log.info("Searching for SensorTag...")
    val advertisement = central.scanner()
        .peripherals
        .first { it.name?.isSensorTag == true }
    Log.info("Found $advertisement")

    val peripheral = central.peripheral(advertisement)
    val sensorTag = SensorTag(peripheral)

    sensorTag.gyro.onEach { Log.info(it.toString()) }.launchIn(this)

    suspend fun connect() {
        Log.info("Connecting...")
        peripheral.connect()
        Log.info("Connected")

        Log.info("Write gyro period")
        sensorTag.writeGyroPeriod(periodMillis = 2550L)
        Log.info("Enable gyro")
        sensorTag.enableGyro()
        Log.info("Enable gyro DONE")
    }

    Log.info("Configuring state listener")
    peripheral.state.onEach {
        Log.info(it.toString())
        if (it is State.Disconnected) {
            Log.info("Waiting 5 seconds to reconnect...")
            delay(5_000L)
            connect() // Auto-reconnect on disconnect.
        }
    }.launchIn(this)

    connect()
}

private val String.isSensorTag: Boolean
    get() = startsWith("SensorTag") || startsWith("CC2650 SensorTag")
