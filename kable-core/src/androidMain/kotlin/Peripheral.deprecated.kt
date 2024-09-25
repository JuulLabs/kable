package com.juul.kable

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.CoroutineScope

@Deprecated(
    message = "Replaced with `Peripheral` builder function (not a CoroutineScope extension function).",
    replaceWith = ReplaceWith("Peripheral(advertisement, builderAction)"),
)
public actual fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral {
    advertisement as ScanResultAndroidAdvertisement
    return peripheral(advertisement.bluetoothDevice, builderAction)
}

@Deprecated(
    message = "Replaced with `Peripheral` builder function (not a CoroutineScope extension function).",
    replaceWith = ReplaceWith("Peripheral(bluetoothDevice, builderAction)"),
)
public fun CoroutineScope.peripheral(
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

@Deprecated(
    message = "Replaced with `Peripheral` builder function (not a CoroutineScope extension function).",
    replaceWith = ReplaceWith("Peripheral(identifier, builderAction)"),
)
public fun CoroutineScope.peripheral(
    identifier: Identifier,
    builderAction: PeripheralBuilderAction = {},
): Peripheral {
    val bluetoothDevice = getBluetoothAdapter().getRemoteDevice(identifier)
    return peripheral(bluetoothDevice, builderAction)
}
