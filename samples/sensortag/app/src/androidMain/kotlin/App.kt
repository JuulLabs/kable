package com.juul.sensortag

import android.app.Application
import com.juul.tuulbox.logging.ConsoleLogger
import com.juul.tuulbox.logging.Log

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.dispatcher.install(ConsoleLogger)
    }
}
