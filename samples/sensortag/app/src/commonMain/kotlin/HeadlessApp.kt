package com.juul.sensortag

import com.juul.kable.Peripheral
import com.juul.kable.State.Disconnected
import com.juul.kable.logs.Logging.Level.Data
import com.juul.khronicle.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.io.IOException
import kotlin.time.Duration.Companion.seconds

suspend fun CoroutineScope.headlessApp() {
    Log.info { "Searching for SensorTag..." }
    val advertisement = SensorTag.scanner.advertisements.first()
    Log.info { "Found $advertisement" }

    val sensorTag = Peripheral(advertisement) {
        logging {
            level = Data
        }
    }.let(::SensorTag)

    sensorTag.gyro.onEach { rotation ->
        Log.info { rotation.toString() }
    }.launchIn(this)

    Log.info { "Configuring auto connector" }
    sensorTag.state.onEach { state ->
        Log.info { "Received state: $state" }
        if (state is Disconnected) {
            try {
                Log.verbose { "Attempting connection" }
                sensorTag.connect()
            } catch (e: IOException) {
                Log.error(e) { "Connect failed." }
                throw e
            }
            Log.verbose { "Waiting to reconnect" }
            delay(2.seconds) // Throttle reconnects so we don't hammer the system if connection immediately drops.
        }
    }.launchIn(this).apply {
        invokeOnCompletion { cause ->
            Log.warn(cause) { "Auto connector complete" }
        }
    }
}
