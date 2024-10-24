package com.juul.sensortag.features.sensor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.juul.sensortag.AppTheme
import com.juul.sensortag.SensorTag
import com.juul.sensortag.bluetooth.rememberSystemControl
import com.juul.sensortag.bluetooth.requirements.rememberBluetoothRequirementsFactory
import com.juul.sensortag.features.components.BluetoothDisabled
import com.juul.sensortag.features.sensor.chart.Sample
import com.juul.sensortag.icons.Battery0Bar
import com.juul.sensortag.icons.Battery1Bar
import com.juul.sensortag.icons.Battery2Bar
import com.juul.sensortag.icons.Battery3Bar
import com.juul.sensortag.icons.Battery4Bar
import com.juul.sensortag.icons.Battery5Bar
import com.juul.sensortag.icons.BatteryFull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.Flow

class SensorScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel()
        val viewState = screenModel.state.collectAsState().value

        AppTheme {
            Column(
                Modifier
                    .background(color = MaterialTheme.colors.background)
                    .fillMaxSize(),
            ) {
                TopAppBar(
                    navigationIcon = {
                        val navigator = LocalNavigator.currentOrThrow
                        IconButton(navigator::pop) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    title = { Text("SensorTag: ${viewState::class.simpleName}") },
                    actions = {
                        if (viewState is ViewState.Connected) {
                            Icon(
                                imageVector = batteryIconForLevel(viewState.battery),
                                contentDescription = "${viewState.battery}% battery level",
                            )
                            Text("${viewState.battery}%")
                            Spacer(Modifier.size(5.dp))
                        }
                    }
                )

                ProvideTextStyle(
                    TextStyle(contentColorFor(MaterialTheme.colors.background))
                ) {
                    val systemControl = rememberSystemControl()
                    when (viewState) {
                        ViewState.BluetoothOff -> BluetoothDisabled(systemControl::requestToTurnBluetoothOn)
                        is ViewState.Connected -> SensorPane(viewState.period, screenModel.data, screenModel::setPeriod)
                        else -> Connecting()
                    }
                }
            }
        }
    }

    @Composable
    private fun rememberScreenModel(): SensorScreenModel {
        val bluetoothRequirementsFactory = rememberBluetoothRequirementsFactory()
        val screenModel = rememberScreenModel {
            val bluetoothRequirements = bluetoothRequirementsFactory.create()
            SensorScreenModel(bluetoothRequirements)
        }
        return screenModel
    }
}

@Composable
private fun SensorPane(
    period: Duration,
    data: Flow<List<Sample>>,
    onPeriodChange: (Duration) -> Unit,
) {
    Column(Modifier.padding(20.dp)) {
        SensorDisplay(data, modifier = Modifier.weight(1f).fillMaxWidth())
        Spacer(modifier = Modifier.size(10.dp))
        PeriodSlider(period, onPeriodChange)
    }
}

@Composable
private fun PeriodSlider(
    period: Duration,
    onPeriodChange: (Duration) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = CenterVertically,
    ) {
        Slider(
            period.inWholeMilliseconds.toFloat(),
            onValueChange = { value -> onPeriodChange(value.toInt().milliseconds) },
            modifier = Modifier.weight(1f),
            valueRange = SensorTag.PeriodRange.inWholeMilliseconds.toFloat(),
        )

        Text(
            "Period: $period",
            modifier = Modifier.padding(start = 15.dp)
        )
    }
}

@Composable
private fun Connecting() {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

private val ClosedRange<Duration>.inWholeMilliseconds: LongRange
    get() = start.inWholeMilliseconds..endInclusive.inWholeMilliseconds

private fun LongRange.toFloat(): ClosedFloatingPointRange<Float> =
    start.toFloat()..endInclusive.toFloat()

private fun batteryIconForLevel(level: Int): ImageVector {
    return when {
        level == 100 -> Icons.Filled.BatteryFull
        level >= 83 -> Icons.Filled.Battery5Bar
        level >= 66 -> Icons.Filled.Battery4Bar
        level >= 50 -> Icons.Filled.Battery3Bar
        level >= 33 -> Icons.Filled.Battery2Bar
        level >= 16 -> Icons.Filled.Battery1Bar
        level >= 0 -> Icons.Filled.Battery0Bar
        else -> error("Unsupported battery level: $level")
    }
}
