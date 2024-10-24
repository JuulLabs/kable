package com.juul.sensortag

import com.juul.khronicle.ConsoleLogger
import com.juul.khronicle.ConstantTagGenerator
import com.juul.khronicle.Log

fun configureLogging() {
    Log.tagGenerator = ConstantTagGenerator(tag = "SensorTag")
    Log.dispatcher.install(ConsoleLogger)
}
