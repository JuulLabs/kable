package com.juul.sensortag

import com.juul.kable.Scanner
import com.juul.kable.State.Disconnected
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

    val peripheral = peripheral(advertisement)
    val sensorTag = SensorTag(peripheral)

    sensorTag.gyro.onEach { rotation ->
        Log.info { rotation.toString() }
    }.launchIn(this)

    suspend fun connect() {
        Log.info { "Connecting..." }
        peripheral.connect()
        Log.info { "Connected" }

        Log.verbose { "Write gyro period" }
        sensorTag.writeGyroPeriod(periodMillis = 2550L)
        Log.info { "Enable gyro" }
        sensorTag.enableGyro()
        Log.info { "Enable gyro DONE" }
    }

    Log.info { "Configuring state listener" }
    peripheral.state.onEach { state ->
        Log.info { state.toString() }
        if (state is Disconnected) {
            Log.info { "Waiting 5 seconds to reconnect..." }
            delay(5_000L)
            connect() // Auto-reconnect on disconnect.
        }
    }.launchIn(this)

    connect()
}

private val String.isSensorTag: Boolean
    get() = startsWith("SensorTag") || startsWith("CC2650 SensorTag")
