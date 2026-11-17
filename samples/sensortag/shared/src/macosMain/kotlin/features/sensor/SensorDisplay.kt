package com.juul.sensortag.features.sensor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.juul.sensortag.features.sensor.chart.Sample
import kotlinx.coroutines.flow.Flow

@Composable
actual fun SensorDisplay(data: Flow<List<Sample>>, modifier: Modifier) {
    error("Sensor display not supported on macOS")
}
