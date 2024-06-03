@file:OptIn(ExperimentalTime::class)

package com.juul.sensortag.features.sensor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.Bluetooth
import com.juul.kable.Bluetooth.Availability.Available
import com.juul.kable.Bluetooth.Availability.Unavailable
import com.juul.kable.ConnectionLostException
import com.juul.kable.NotReadyException
import com.juul.kable.Peripheral
import com.juul.kable.State
import com.juul.kable.peripheral
import com.juul.khronicle.Log
import com.juul.sensortag.Sample
import com.juul.sensortag.SensorTag
import com.juul.sensortag.Vector3f
import com.juul.sensortag.features.sensor.ViewState.Connected.GyroState
import com.juul.sensortag.features.sensor.ViewState.Connected.GyroState.AxisState
import com.juul.sensortag.peripheralScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private val reconnectDelay = 1.seconds

sealed class ViewState {

    data object BluetoothUnavailable : ViewState()

    data object Connecting : ViewState()

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
                val degreesPerSecond: Float,
                val progress: Float,
            )
        }
    }

    data object Disconnecting : ViewState()

    data object Disconnected : ViewState()
}

val ViewState.label: String
    get() = when (this) {
        ViewState.BluetoothUnavailable -> "Bluetooth unavailable"
        ViewState.Connecting -> "Connecting"
        is ViewState.Connected -> "Connected"
        ViewState.Disconnecting -> "Disconnecting"
        ViewState.Disconnected -> "Disconnected"
    }

class SensorViewModel(
    application: Application,
    macAddress: String
) : AndroidViewModel(application) {

    private val autoConnect = MutableStateFlow(false)

    // Intermediary scope needed until https://github.com/JuulLabs/kable/issues/577 is resolved.
    private val scope = CoroutineScope(peripheralScope.coroutineContext + Job(peripheralScope.coroutineContext.job))

    private val peripheral = scope.peripheral(macAddress) {
        autoConnectIf(autoConnect::value)
    }
    private val sensorTag = SensorTag(peripheral)
    private val state = combine(Bluetooth.availability, peripheral.state, ::Pair)

    private val periodProgress = AtomicInteger()

    private var startTime: TimeMark? = null

    val data = sensorTag.gyro
        .onStart { startTime = TimeSource.Monotonic.markNow() }
        .scan(emptyList<Sample>()) { accumulator, value ->
            val t = startTime!!.elapsedNow().inWholeMilliseconds / 1000f
            accumulator.takeLast(50) + Sample(t, value.x, value.y, value.z)
        }
        .filter { it.size > 3 }

    init {
        viewModelScope.enableAutoReconnect()
    }

    private fun CoroutineScope.enableAutoReconnect() {
        state.filter { (bluetoothAvailability, connectionState) ->
            bluetoothAvailability == Available && connectionState is State.Disconnected
        }.onEach {
            ensureActive()
            Log.info { "Waiting $reconnectDelay to reconnect..." }
            delay(reconnectDelay)
            connect()
        }.launchIn(this)
    }

    private fun CoroutineScope.connect() {
        launch {
            Log.debug { "Connecting" }
            try {
                peripheral.connect()
                autoConnect.value = true
                sensorTag.enableGyro()
                sensorTag.writeGyroPeriodProgress(periodProgress.get())
            } catch (e: ConnectionLostException) {
                autoConnect.value = false
                Log.warn(e) { "Connection attempt failed" }
            }
        }
    }

    val viewState: Flow<ViewState> = state
        .flatMapLatest { (bluetoothAvailability, state) ->
            if (bluetoothAvailability is Unavailable) {
                return@flatMapLatest flowOf(ViewState.BluetoothUnavailable)
            }
            when (state) {
                is State.Connecting -> flowOf(ViewState.Connecting)
                State.Connected -> combine(
                    peripheral.remoteRssi(),
                    sensorTag.gyro
                ) { rssi, gyro ->
                    ViewState.Connected(rssi, gyroState(gyro))
                }

                State.Disconnecting -> flowOf(ViewState.Disconnecting)
                is State.Disconnected -> flowOf(ViewState.Disconnected)
            }
        }

    private val max = Max()
    private fun gyroState(gyro: Vector3f): GyroState {
        val progress = gyro.progress(max.maxOf(gyro))
        return GyroState(
            x = AxisState(degreesPerSecond = gyro.x, progress = progress.x),
            y = AxisState(degreesPerSecond = gyro.y, progress = progress.y),
            z = AxisState(degreesPerSecond = gyro.z, progress = progress.z),
        )
    }

    fun setPeriod(progress: Int) {
        periodProgress.set(progress)
        viewModelScope.launch {
            sensorTag.writeGyroPeriodProgress(progress)
        }
    }

    override fun onCleared() {
        peripheralScope.launch {
            viewModelScope.coroutineContext.job.join()
            peripheral.disconnect()
            scope.cancel()
        }
    }
}

private fun Peripheral.remoteRssi() = flow {
    while (true) {
        val rssi = rssi()
        Log.debug { "RSSI: $rssi" }
        emit(rssi)
        delay(1_000L)
    }
}.catch { cause ->
    // todo: Investigate better way of handling this failure case.
    // When disconnecting, we may attempt to read `rssi` causing a `NotReadyException` but the hope is that `remoteRssi`
    // Flow would already be cancelled by the time the `Peripheral` is "not ready" (doesn't seem to be the case).
    if (cause !is NotReadyException) throw cause
}

private suspend fun SensorTag.writeGyroPeriodProgress(progress: Int) {
    val period = progress / 100f * (2550 - 100) + 100
    Log.verbose { "period = $period" }
    writeGyroPeriod(period.toLong())
}

private fun Vector3f.progress(max: Max) = Vector3f(
    if (max.x != 0f) x.absoluteValue / max.x else 0f,
    if (max.y != 0f) y.absoluteValue / max.y else 0f,
    if (max.z != 0f) z.absoluteValue / max.z else 0f,
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
