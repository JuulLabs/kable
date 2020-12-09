package com.juul.kable

import android.content.Context
import androidx.startup.Initializer

internal lateinit var applicationContext: Context
    private set

public object Kable

public class KableInitializer : Initializer<Kable> {

    override fun create(context: Context): Kable {
        applicationContext = context.applicationContext
        return Kable
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
