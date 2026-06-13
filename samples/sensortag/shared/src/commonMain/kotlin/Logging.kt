package com.juul.sensortag

import com.juul.khronicle.ConsoleLogger
import com.juul.khronicle.Log

fun configureLogging() {
    Log.dispatcher.install(ConsoleLogger)
}
