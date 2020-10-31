package com.juul.kable

import com.juul.kable.external.Bluetooth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlin.coroutines.CoroutineContext

public fun CoroutineScope.central(): Central = JsCentral(coroutineContext)

public class JsCentral internal constructor(
    parentCoroutineContext: CoroutineContext
) : Central {

    private val scope = CoroutineScope(parentCoroutineContext + Job(parentCoroutineContext[Job]))
    private val bluetooth: Bluetooth? = js("window.navigator.bluetooth")

    public override fun scanner(): Scanner {
        TODO("Not yet implemented")
    }

    public suspend fun requestPeripheral(options: Options): Peripheral {
        bluetooth ?: error("Bluetooth unavailable")
        val device = bluetooth.requestDevice(options.toDynamic()).await() // fixme: !!
        return scope.peripheral(device)
    }

    public override fun peripheral(advertisement: Advertisement): Peripheral {
        TODO("Not yet implemented")
    }
}

private fun Options.toDynamic(): dynamic = if (filters == null) {
    object {
        val acceptAllDevices = true
        val optionalServices = this@toDynamic.optionalServices
    }
} else {
    object {
        val optionalServices = this@toDynamic.optionalServices
        val filters = this@toDynamic.filters
    }
}
