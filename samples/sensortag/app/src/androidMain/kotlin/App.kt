package com.juul.sensortag

import android.app.Application
import com.juul.kable.AndroidCentral
import com.juul.kable.central
import kotlinx.coroutines.GlobalScope

lateinit var central: AndroidCentral

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        central = GlobalScope.central(this)
    }
}
