package com.juul.kable

import com.juul.kable.external.Bluetooth
import kotlinx.coroutines.CoroutineScope
import kotlin.js.Promise

private val bluetooth: Bluetooth
    get() = js("window.navigator.bluetooth") ?: error("Bluetooth unavailable")

public fun CoroutineScope.requestPeripheral(
    options: Options
): Promise<Peripheral> = bluetooth
    .requestDevice(options.toDynamic())
    .then { device -> peripheral(device) }

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
