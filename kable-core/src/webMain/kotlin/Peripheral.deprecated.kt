package com.juul.kable

import com.juul.kable.external.BluetoothDevice
import kotlinx.coroutines.CoroutineScope

@Deprecated(
    message = "Replaced with `Peripheral` builder function (not a CoroutineScope extension function).",
    replaceWith = ReplaceWith("Peripheral(advertisement, builderAction)"),
    level = DeprecationLevel.ERROR,
)
public actual fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral {
    advertisement as BluetoothAdvertisingEventWebBluetoothAdvertisement
    return peripheral(advertisement.bluetoothDevice, builderAction)
}

@Deprecated(
    message = "Replaced with `Peripheral` builder function (not a CoroutineScope extension function).",
    replaceWith = ReplaceWith("Peripheral(bluetoothDevice, builderAction)"),
    level = DeprecationLevel.WARNING,
)
internal fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    builderAction: PeripheralBuilderAction = {},
): WebBluetoothPeripheral = peripheral(bluetoothDevice, PeripheralBuilder().apply(builderAction))

internal fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    builder: PeripheralBuilder,
): WebBluetoothPeripheral = builder.build(bluetoothDevice)
