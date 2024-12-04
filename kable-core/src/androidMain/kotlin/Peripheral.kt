package com.juul.kable

import android.bluetooth.BluetoothDevice

public actual fun Peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral {
    advertisement as ScanResultAndroidAdvertisement
    return Peripheral(advertisement.bluetoothDevice, builderAction)
}

/** @throws IllegalStateException If bluetooth is not supported. */
public fun Peripheral(
    identifier: Identifier,
    builderAction: PeripheralBuilderAction = {},
): Peripheral = Peripheral(getBluetoothAdapter().getRemoteDevice(identifier), builderAction)

public fun Peripheral(
    bluetoothDevice: BluetoothDevice,
    builderAction: PeripheralBuilderAction = {},
): Peripheral {
    val builder = PeripheralBuilder().apply(builderAction)
    return BluetoothDeviceAndroidPeripheral(
        bluetoothDevice,
        builder.autoConnectPredicate,
        builder.transport,
        builder.phy,
        builder.threadingStrategy,
        builder.observationExceptionHandler,
        builder.onServicesDiscovered,
        builder.logging,
        builder.disconnectTimeout,
    )
}
