package com.juul.kable

import kotlinx.coroutines.CoroutineScope
import kotlin.js.Promise

@Deprecated(
    message = "Deprecated in favor of `suspend` version of function.",
    replaceWith = ReplaceWith("requestPeripheral(options, scope) { }"),
)
public fun CoroutineScope.requestPeripheral(
    options: Options,
    builderAction: PeripheralBuilderAction = {},
): Promise<Peripheral> = bluetoothDeprecated
    .requestDevice(options.toRequestDeviceOptions())
    .then { device -> peripheral(device, builderAction) }
