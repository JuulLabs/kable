package com.juul.sensortag.features.sensor

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.juul.kable.State
import com.juul.khronicle.Log
import com.juul.sensortag.SensorTag
import com.juul.sensortag.bluetooth.requirements.BluetoothRequirements
import com.juul.sensortag.bluetooth.requirements.Deficiency.BluetoothOff
import com.juul.sensortag.coroutines.flow.withStartTime
import com.juul.sensortag.features.sensor.chart.Sample
import com.juul.sensortag.peripheral
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val reconnectDelay = 1.seconds

class SensorScreenModel(
    bluetoothRequirements: BluetoothRequirements,
) : ScreenModel {

    private val sensorTag = peripheral?.let(::SensorTag) ?: error("Peripheral not set")

    @OptIn(ExperimentalCoroutinesApi::class)
    val state = bluetoothRequirements.deficiencies
        .map { BluetoothOff in it }
        .distinctUntilChanged()
        .flatMapLatest { isBluetoothOff ->
            if (isBluetoothOff) {
                flowOf(ViewState.BluetoothOff)
            } else {
                sensorTag.state.flatMapLatest { state ->
                    when (state) {
                        is State.Connecting -> flowOf(ViewState.Connecting)
                        is State.Connected -> combine(
                            sensorTag.battery,
                            sensorTag.rssi,
                            sensorTag.periodMillis,
                            ViewState::Connected,
                        )

                        is State.Disconnecting -> flowOf(ViewState.Disconnecting)
                        is State.Disconnected -> flowOf(ViewState.Disconnected)
                    }
                }
            }
        }
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(), ViewState.Connecting)

    val data = sensorTag.gyro
        .withStartTime()
        .scan(emptyList<Sample>()) { accumulator, (start, value) ->
            val t = start.elapsedNow().inWholeMilliseconds / 1_000f
            accumulator.takeLast(50) + Sample(t, value.x, value.y, value.z)
        }

    init {
        onDisconnected {
            Log.info { "Waiting $reconnectDelay to reconnect..." }
            delay(reconnectDelay)
            sensorTag.connect()
        }
    }

    fun setPeriod(period: Duration) {
        screenModelScope.launch {
            sensorTag.setPeriod(period)
        }
    }

    override fun onDispose() {
        // GlobalScope to allow disconnect process to continue after leaving screen.
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            sensorTag.disconnect()
        }
    }

    private fun onDisconnected(action: suspend (ViewState.Disconnected) -> Unit) {
        state
            .filterIsInstance<ViewState.Disconnected>()
            .onEach(action)
            .launchIn(screenModelScope)
    }
}
