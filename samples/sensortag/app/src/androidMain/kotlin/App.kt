package com.juul.sensortag

import android.app.Application
import com.juul.khronicle.ConsoleLogger
import com.juul.khronicle.Log

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.dispatcher.install(ConsoleLogger)
    }
}
