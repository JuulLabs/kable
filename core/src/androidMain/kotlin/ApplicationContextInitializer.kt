package com.juul.kable

import android.content.Context
import androidx.startup.Initializer

internal lateinit var applicationContext: Context

public class ApplicationContextInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        applicationContext = context.applicationContext
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
