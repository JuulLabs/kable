package com.juul.sensortag.features.sensor

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.juul.exercise.annotations.Exercise
import com.juul.exercise.annotations.Extra
import com.juul.sensortag.databinding.SensorBinding
import com.juul.sensortag.features.sensor.ViewState.Connected
import com.juul.sensortag.observe
import com.juul.sensortag.onStopTracking

@Exercise(Extra("macAddress", String::class))
class SensorActivity : AppCompatActivity() {

    private val viewModel by viewModels<SensorViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = SensorViewModel(application, extras.macAddress) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SensorBinding.inflate(layoutInflater).apply {
            period.onStopTracking { viewModel.setPeriod(progress) }

            observe(viewModel.viewState.asLiveData()) { viewState ->
                period.isEnabled = viewState is Connected

                if (viewState is Connected) {
                    status.text = "${viewState.label} (${viewState.rssi} dBm)"
                    with(viewState.gyro) {
                        xAxisLabel.text = x.label
                        yAxisLabel.text = y.label
                        zAxisLabel.text = z.label
                        xAxisBar.progress = x.progress
                        yAxisBar.progress = y.progress
                        zAxisBar.progress = z.progress
                    }
                } else {
                    status.text = viewState.label
                    xAxisLabel.text = null
                    yAxisLabel.text = null
                    zAxisLabel.text = null
                    xAxisBar.progress = 0
                    yAxisBar.progress = 0
                    zAxisBar.progress = 0
                }
            }

            setContentView(root)
        }
    }
}
