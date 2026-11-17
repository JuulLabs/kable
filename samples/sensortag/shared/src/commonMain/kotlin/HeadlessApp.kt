package com.juul.sensortag

import com.juul.kable.Peripheral
import com.juul.kable.State.Disconnected
import com.juul.kable.logs.Logging.Level.Data
import com.juul.khronicle.Log
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.io.IOException

private const val TAG = "App/Headless"

suspend fun CoroutineScope.headlessApp() {
    Log.info(tag = TAG) { "Searching for SensorTag..." }
    val advertisement = SensorTag.scanner.advertisements.first()
    Log.info(tag = TAG) { "Found $advertisement" }

    val sensorTag = Peripheral(advertisement) {
        logging {
            level = Data
        }
    }.let(::SensorTag)

    sensorTag.gyro.onEach { rotation ->
        Log.info(tag = TAG) { rotation.toString() }
    }.launchIn(this)

    Log.info(tag = TAG) { "Configuring auto connector" }
    sensorTag.state.onEach { state ->
        Log.info(tag = TAG) { "Received state: $state" }
        if (state is Disconnected) {
            try {
                Log.verbose(tag = TAG) { "Attempting connection" }
                sensorTag.connect()
            } catch (e: IOException) {
                Log.error(tag = TAG, throwable = e) { "Connect failed." }
                throw e
            }
            Log.verbose(tag = TAG) { "Waiting to reconnect" }
            delay(2.seconds) // Throttle reconnects so we don't hammer the system if connection immediately drops.
        }
    }.launchIn(this).apply {
        invokeOnCompletion { cause ->
            Log.warn(tag = TAG, throwable = cause) { "Auto connector complete" }
        }
    }
}
