package com.juul.kable

import kotlinx.coroutines.CoroutineScope
import kotlin.js.Promise

public fun CoroutineScope.requestPeripheral(
    options: Options,
    builderAction: PeripheralBuilderAction = {},
): Promise<Peripheral> = bluetooth
    .requestDevice(options.toDynamic())
    .then { device -> peripheral(device, builderAction) }

/**
 * Converts [Options] to JavaScript friendly object.
 *
 * According to the `requestDevice`
 * [example](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/requestDevice#example), the form of the
 * JavaScript object should be similar to:
 * ```
 * let options = {
 *   filters: [
 *     {services: ['heart_rate']},
 *     {services: [0x1802, 0x1803]},
 *     {services: ['c48e6067-5295-48d3-8d5c-0395f61792b1']},
 *     {name: 'ExampleName'},
 *     {namePrefix: 'Prefix'}
 *   ],
 *   optionalServices: ['battery_service']
 * }
 * ```
 */
private fun Options.toDynamic(): dynamic = if (filters == null) {
    jso {
        this.acceptAllDevices = true
        this.optionalServices = optionalServices
    }
} else {
    jso {
        this.optionalServices = optionalServices
        this.filters = filters.map { it.toDynamic() }.toTypedArray()
    }
}

private fun Options.Filter.toDynamic(): dynamic =
    when (this) {
        is Options.Filter.Name -> jso { this.name = name }
        is Options.Filter.NamePrefix -> jso { this.namePrefix = namePrefix }
        is Options.Filter.Services -> jso { this.services = services }
    }
