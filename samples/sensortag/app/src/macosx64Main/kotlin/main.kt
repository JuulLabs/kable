package com.juul.sensortag

import com.juul.kable.Scanner
import com.juul.kable.State.Disconnected
import com.juul.kable.logs.Logging.Level.Data
import com.juul.kable.peripheral
import com.juul.tuulbox.logging.ConsoleLogger
import com.juul.tuulbox.logging.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    Log.dispatcher.install(ConsoleLogger)

    Log.info { "Searching for SensorTag..." }
    val advertisement = Scanner()
        .advertisements
        .first { it.name?.isSensorTag == true }
    Log.info { "Found $advertisement" }

    val peripheral = peripheral(advertisement) {
        logging {
            level = Data
        }
    }
    val sensorTag = SensorTag(peripheral)

    sensorTag.gyro.onEach { rotation ->
        Log.info { rotation.toString() }
    }.launchIn(this)

    suspend fun connect() {
        Log.info { "Connecting..." }
        peripheral.connect()
        Log.info { "Connected" }

        Log.verbose { "Writing gyro period" }
        sensorTag.writeGyroPeriod(periodMillis = 2550L)
        Log.info { "Enabling gyro" }
        sensorTag.enableGyro()
        Log.info { "Gyro enabled" }
    }

    Log.info { "Configuring auto connector" }
    peripheral.state.onEach { state ->
        Log.info { state.toString() }
        if (state is Disconnected) {
            connect()
            delay(5_000L) // Throttle reconnects so we don't hammer the system if connection immediately drops.
        }
    }.launchIn(this)
}

private val String.isSensorTag: Boolean
    get() = startsWith("SensorTag") || startsWith("CC2650 SensorTag")
