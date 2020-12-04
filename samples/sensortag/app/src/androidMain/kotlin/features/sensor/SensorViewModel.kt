package com.juul.sensortag.features.sensor

import android.app.Application
import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.Peripheral
import com.juul.kable.State
import com.juul.sensortag.Log
import com.juul.sensortag.SensorTag
import com.juul.sensortag.TAG
import com.juul.sensortag.Vector3f
import com.juul.sensortag.central
import com.juul.sensortag.features.sensor.ViewState.Connected.GyroState
import com.juul.sensortag.features.sensor.ViewState.Connected.GyroState.AxisState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt

private val DISCONNECT_TIMEOUT = TimeUnit.SECONDS.toMillis(5)

sealed class ViewState {

    object Connecting : ViewState()

    data class Connected(
        val rssi: Int,
        val gyro: GyroState
    ) : ViewState() {

        data class GyroState(
            val x: AxisState,
            val y: AxisState,
            val z: AxisState
        ) {

            data class AxisState(
                val label: CharSequence,
                val progress: Int
            )
        }
    }

    object Disconnecting : ViewState()

    object Disconnected : ViewState()
}

val ViewState.label: CharSequence
    get() = when (this) {
        is ViewState.Connecting -> "Connecting"
        is ViewState.Connected -> "Connected"
        is ViewState.Disconnecting -> "Disconnecting"
        is ViewState.Disconnected -> "Disconnected"
    }

class SensorViewModel(
    application: Application,
    macAddress: String
) : AndroidViewModel(application) {

    private val peripheral = central.peripheral(bluetoothDeviceFrom(macAddress))
    private val sensorTag = SensorTag(peripheral)
    private val connectionAttempt = AtomicInteger()

    private val periodProgress = AtomicInteger()

    init {
        viewModelScope.enableAutoReconnect()
        viewModelScope.connect()
    }

    private fun CoroutineScope.enableAutoReconnect() {
        peripheral.state
            .filter { it is State.Disconnected }
            .onEach {
                val timeMillis =
                    backoff(base = 500L, multiplier = 2f, retry = connectionAttempt.getAndIncrement())
                Log.info("Waiting $timeMillis ms to reconnect...")
                delay(timeMillis)
                connect()
            }
            .launchIn(this)
    }

    private fun CoroutineScope.connect() {
        connectionAttempt.incrementAndGet()
        launch {
            Log.debug("connect")
            peripheral.connect()
            sensorTag.enableGyro()
            sensorTag.writeGyroPeriodProgress(periodProgress.get())
            connectionAttempt.set(0)
        }
    }

    val viewState: Flow<ViewState> = peripheral.state.flatMapLatest { state ->
        when (state) {
            State.Connecting -> flowOf(ViewState.Connecting)
            State.Connected -> combine(peripheral.remoteRssi(), sensorTag.gyro) { rssi, gyro ->
                ViewState.Connected(rssi, gyroState(gyro))
            }
            State.Disconnecting -> flowOf(ViewState.Disconnecting)
            is State.Disconnected -> flowOf(ViewState.Disconnected)
        }
    }

    private val max = Max()
    private fun gyroState(gyro: Vector3f): GyroState {
        val (progressX, progressY, progressZ) = gyro.progress(max.maxOf(gyro))
        return GyroState(
            x = AxisState(label = "X: ${gyro.x} ˚/sec", progress = progressX),
            y = AxisState(label = "Y: ${gyro.y} ˚/sec", progress = progressY),
            z = AxisState(label = "Z: ${gyro.z} ˚/sec", progress = progressZ)
        )
    }

    fun setPeriod(progress: Int) {
        periodProgress.set(progress)
        viewModelScope.launch {
            sensorTag.writeGyroPeriodProgress(progress)
        }
    }

    override fun onCleared() {
        GlobalScope.launch {
            withTimeoutOrNull(DISCONNECT_TIMEOUT) {
                peripheral.disconnect()
            }
        }
    }
}

private fun bluetoothDeviceFrom(macAddress: String) =
    BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress)

private fun Peripheral.remoteRssi() = flow {
    while (true) {
        val rssi = rssi()
        Log.debug("RSSI: $rssi")
        emit(rssi)
        delay(1_000L)
    }
}

private suspend fun SensorTag.writeGyroPeriodProgress(progress: Int) {
    val period = progress / 100f * (2550 - 100) + 100
    Log.verbose("period = $period")
    writeGyroPeriod(period.toLong())
}

private fun Vector3f.progress(max: Max) = Triple(
    ((if (max.x != 0f) x.absoluteValue / max.x else 0f) * 100).roundToInt(),
    ((if (max.y != 0f) y.absoluteValue / max.y else 0f) * 100).roundToInt(),
    ((if (max.z != 0f) z.absoluteValue / max.z else 0f) * 100).roundToInt()
)

private data class Max(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
) {
    fun maxOf(vector: Vector3f) = apply {
        x = maxOf(x, vector.x.absoluteValue)
        y = maxOf(y, vector.y.absoluteValue)
        z = maxOf(z, vector.z.absoluteValue)
    }
}

/**
 * Exponential backoff using the following formula:
 *
 * ```
 * delay = base * multiplier ^ retry
 * ```
 *
 * For example (using `base = 100` and `multiplier = 2`):
 *
 * | retry | delay |
 * |-------|-------|
 * |   1   |   100 |
 * |   2   |   200 |
 * |   3   |   400 |
 * |   4   |   800 |
 * |   5   |  1600 |
 * |  ...  |   ... |
 *
 * Inspired by:
 * [Exponential Backoff And Jitter](https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/)
 *
 * @return Backoff delay (in units matching [base] units, e.g. if [base] units are milliseconds then returned delay will be milliseconds).
 */
private fun backoff(
    base: Long,
    multiplier: Float,
    retry: Int,
): Long = (base * multiplier.pow(retry - 1)).toLong()
