package com.juul.kable

import platform.CoreBluetooth.CBPeripheral

public actual fun Peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral {
    advertisement as CBPeripheralCoreBluetoothAdvertisement
    return Peripheral(advertisement.cbPeripheral, builderAction)
}

@Suppress("FunctionName") // Builder function.
public fun Peripheral(
    identifier: Identifier,
    builderAction: PeripheralBuilderAction = {},
): CoreBluetoothPeripheral {
    val cbPeripheral = CentralManager.Default.retrievePeripheral(identifier)
        ?: throw NoSuchElementException("Peripheral with UUID $identifier not found")
    return Peripheral(cbPeripheral, builderAction)
}

@Suppress("FunctionName") // Builder function.
public fun Peripheral(
    cbPeripheral: CBPeripheral,
    builderAction: PeripheralBuilderAction = {},
): CoreBluetoothPeripheral {
    val builder = PeripheralBuilder().apply(builderAction)
    return CBPeripheralCoreBluetoothPeripheral(
        cbPeripheral,
        builder.observationExceptionHandler,
        builder.onServicesDiscovered,
        builder.logging,
        builder.disconnectTimeout,
    )
}
