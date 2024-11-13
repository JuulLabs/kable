package com.juul.kable

import com.juul.kable.external.BluetoothDevice

public actual fun Peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral {
    advertisement as BluetoothAdvertisingEventWebBluetoothAdvertisement
    return Peripheral(advertisement.bluetoothDevice, builderAction)
}

@Suppress("FunctionName") // Builder function.
internal fun Peripheral(
    bluetoothDevice: BluetoothDevice,
    builderAction: PeripheralBuilderAction,
): WebBluetoothPeripheral = Peripheral(bluetoothDevice, PeripheralBuilder().apply(builderAction))

@Suppress("FunctionName") // Builder function.
internal fun Peripheral(
    bluetoothDevice: BluetoothDevice,
    builder: PeripheralBuilder,
): WebBluetoothPeripheral = builder.build(bluetoothDevice)
