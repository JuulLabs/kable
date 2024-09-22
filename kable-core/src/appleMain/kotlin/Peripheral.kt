package com.juul.kable

import kotlinx.coroutines.CoroutineScope
import platform.CoreBluetooth.CBPeripheral
import kotlin.uuid.ExperimentalUuidApi

public actual fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral {
    advertisement as CBPeripheralCoreBluetoothAdvertisement
    return peripheral(advertisement.cbPeripheral, builderAction)
}

@OptIn(ExperimentalUuidApi::class)
public fun CoroutineScope.peripheral(
    identifier: Identifier,
    builderAction: PeripheralBuilderAction = {},
): Peripheral {
    val cbPeripheral = CentralManager.Default.retrievePeripheral(identifier)
        ?: throw NoSuchElementException("Peripheral with UUID $identifier not found")
    return peripheral(cbPeripheral, builderAction)
}

public fun CoroutineScope.peripheral(
    cbPeripheral: CBPeripheral,
    builderAction: PeripheralBuilderAction,
): CoreBluetoothPeripheral {
    val builder = PeripheralBuilder()
    builder.builderAction()
    return CBPeripheralCoreBluetoothPeripheral(
        coroutineContext,
        cbPeripheral,
        builder.observationExceptionHandler,
        builder.onServicesDiscovered,
        builder.logging,
    )
}
