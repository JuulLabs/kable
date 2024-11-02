package com.juul.kable

import kotlinx.coroutines.CoroutineScope
import platform.CoreBluetooth.CBPeripheral
import kotlin.uuid.ExperimentalUuidApi

@Deprecated(
    message = "Replaced with `Peripheral` builder function (not a CoroutineScope extension function).",
    replaceWith = ReplaceWith("Peripheral(cbPeripheral, builderAction)"),
)
public actual fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral = Peripheral(advertisement, builderAction)

@OptIn(ExperimentalUuidApi::class)
@Deprecated(
    message = "Replaced with `Peripheral` builder function (not a CoroutineScope extension function).",
    replaceWith = ReplaceWith("Peripheral(cbPeripheral, builderAction)"),
)
public fun CoroutineScope.peripheral(
    identifier: Identifier,
    builderAction: PeripheralBuilderAction = {},
): Peripheral = Peripheral(identifier, builderAction)

@Deprecated(
    message = "Replaced with `Peripheral` builder function (not a CoroutineScope extension function).",
    replaceWith = ReplaceWith("Peripheral(cbPeripheral, builderAction)"),
)
public fun CoroutineScope.peripheral(
    cbPeripheral: CBPeripheral,
    builderAction: PeripheralBuilderAction,
): CoreBluetoothPeripheral = Peripheral(cbPeripheral, builderAction)
