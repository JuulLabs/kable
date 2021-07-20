package com.juul.kable

import com.juul.kable.external.Bluetooth
import kotlinext.js.jsObject
import kotlinx.coroutines.CoroutineScope
import kotlin.js.Promise

internal val bluetooth: Bluetooth
    get() = checkNotNull(js("window.navigator.bluetooth") as? Bluetooth) { "Bluetooth unavailable" }

public fun CoroutineScope.requestPeripheral(
    options: Options,
    builderAction: PeripheralBuilderAction = {},
): Promise<Peripheral> = bluetooth
    .requestDevice(options.toDynamic())
    .then { device -> peripheral(device, builderAction) }

private fun Options.toDynamic(): dynamic = if (filters == null) {
    jsObject {
        this.acceptAllDevices = true
        this.optionalServices = optionalServices
    }
} else {
    jsObject {
        this.optionalServices = optionalServices
        this.filters = filters
    }
}
