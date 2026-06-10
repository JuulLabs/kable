package com.juul.sensortag.features.sensor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.juul.krayon.compose.ElementView
import com.juul.sensortag.features.sensor.chart.Sample
import com.juul.sensortag.features.sensor.chart.update
import kotlinx.coroutines.flow.Flow

@Composable
actual fun SensorDisplay(data: Flow<List<Sample>>, modifier: Modifier) {
    ElementView(data, ::update, modifier)
}
