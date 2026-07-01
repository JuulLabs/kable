package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult

public actual fun Peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral {
    advertisement as ScanResultAndroidAdvertisement
    return Peripheral(advertisement.bluetoothDevice, builderAction)
}

@ExperimentalApi // Experimental while evaluating if this API introduces any footguns.
public fun Peripheral(
    scanResult: ScanResult,
    builderAction: PeripheralBuilderAction,
): Peripheral = Peripheral(scanResult.device, builderAction)

/** @throws IllegalStateException If bluetooth is not supported. */
public fun Peripheral(
    identifier: Identifier,
    builderAction: PeripheralBuilderAction = {},
): Peripheral = Peripheral(getBluetoothAdapter().getRemoteDevice(identifier.uppercase()), builderAction)

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
