package com.juul.sensortag

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.juul.khronicle.ConsoleLogger
import com.juul.khronicle.Log

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Sensortag"
    ) {
        DisposableEffect(Unit) {
            Log.dispatcher.install(ConsoleLogger)
            onDispose { Log.dispatcher.clear() }
        }
        App()
    }
}
