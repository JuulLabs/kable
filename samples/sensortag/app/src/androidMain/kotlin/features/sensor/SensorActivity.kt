package com.juul.sensortag.features.sensor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.juul.exercise.annotations.Exercise
import com.juul.exercise.annotations.Extra
import com.juul.krayon.element.view.ElementView
import com.juul.krayon.element.view.ElementViewAdapter
import com.juul.sensortag.AppTheme
import com.juul.sensortag.features.sensor.ViewState.Disconnected
import com.juul.sensortag.sensorChart
import kotlin.math.roundToInt


@Exercise(Extra("macAddress", String::class))
class SensorActivity : ComponentActivity() {

    private val viewModel by viewModels<SensorViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = SensorViewModel(application, extras.macAddress) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Column(
                    Modifier
                        .background(color = MaterialTheme.colors.background)
                        .fillMaxSize()) {
                    TopAppBar(title = { Text("SensorTag Example") })

                    ProvideTextStyle(
                        TextStyle(color = contentColorFor(backgroundColor = MaterialTheme.colors.background))
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            val viewState = viewModel.viewState.collectAsState(Disconnected).value

                            Text(viewState.label, fontSize = 18.sp)
                            Spacer(Modifier.size(10.dp))

                            AndroidView(
                                modifier = Modifier.weight(1f),
                                factory = { context ->
                                    ElementView(context).apply {
                                        adapter = ElementViewAdapter(
                                            dataSource = viewModel.data,
                                            updater = ::sensorChart,
                                        )
                                    }
                                })

                            Spacer(Modifier.size(20.dp))
                            Text("Period:")

                            var sliderPosition by remember { mutableFloatStateOf(0f) }
                            Slider(
                                value = sliderPosition,
                                valueRange = 0f..100f,
                                onValueChange = { sliderPosition = it },
                                onValueChangeFinished = { viewModel.setPeriod(sliderPosition.roundToInt()) },
                            )
                        }
                    }
                }
            }
        }
    }
}
