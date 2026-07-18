package com.juul.kable.server

import android.content.Context
import androidx.startup.Initializer

internal lateinit var applicationContext: Context
    private set

public object KableServer

public class KableServerInitializer : Initializer<KableServer> {

    override fun create(context: Context): KableServer {
        applicationContext = context.applicationContext
        return KableServer
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
