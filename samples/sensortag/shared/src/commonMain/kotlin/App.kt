package com.juul.sensortag

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.juul.sensortag.features.scan.ScanScreen

@Composable
fun App() {
    MaterialTheme {
        Navigator(ScanScreen())
    }
}
