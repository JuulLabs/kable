@file:OptIn(ExperimentalKableApi::class, ExperimentalUuidApi::class)

package com.juul.kable.sample.gattserver

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.ExperimentalKableApi
import com.juul.kable.characteristic
import com.juul.kable.characteristicOf
import com.juul.kable.logs.Logging.Level.Events
import com.juul.kable.server.GattServer
import com.juul.kable.service
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TAG = "GattServerSample"

private val heartRateService = Uuid.service("heart_rate")
private val heartRateMeasurement = Uuid.characteristic("heart_rate_measurement")
private val bodySensorLocation = Uuid.characteristic("body_sensor_location")

class ServerViewModel : ViewModel() {

    private val _heartRate = MutableStateFlow(60)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    private val server = GattServer {
        logging {
            level = Events
        }

        service(heartRateService) {
            characteristic(heartRateMeasurement) {
                onSubscription {
                    while (true) {
                        send(heartRateMeasurementOf(heartRate.value))
                        delay(1.seconds)
                    }
                }
            }

            characteristic(bodySensorLocation) {
                value = byteArrayOf(0x01) // Chest.
            }
        }
    }

    val state: StateFlow<GattServer.State> = server.state

    val subscribers = server.subscribers(characteristicOf(heartRateService, heartRateMeasurement))

    private val _advertising = MutableStateFlow(false)
    val advertising: StateFlow<Boolean> = _advertising.asStateFlow()
    private var advertiseJob: Job? = null

    fun start() {
        viewModelScope.launch {
            try {
                server.start()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Failed to start GATT server", e)
            }
        }
    }

    fun stop() {
        viewModelScope.launch {
            server.stop()
        }
    }

    fun toggleAdvertising() {
        val job = advertiseJob
        if (job != null) {
            job.cancel() // Cancelling the `advertise` coroutine stops advertising.
            return
        }
        advertiseJob = viewModelScope.launch {
            _advertising.value = true
            try {
                server.advertise {
                    services = listOf(heartRateService)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Failed to advertise", e)
            } finally {
                _advertising.value = false
                advertiseJob = null
            }
        }
    }

    fun setHeartRate(bpm: Int) {
        _heartRate.value = bpm
    }

    override fun onCleared() {
        server.close()
    }
}

/**
 * Heart Rate Measurement characteristic value: flags (bit 0 clear = heart rate value format is
 * `UINT8`) followed by the heart rate measurement (in beats per minute).
 */
private fun heartRateMeasurementOf(bpm: Int): ByteArray =
    byteArrayOf(0x00, bpm.coerceIn(0, 255).toByte())
