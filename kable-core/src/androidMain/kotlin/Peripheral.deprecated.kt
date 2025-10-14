package com.juul.kable

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job

@Deprecated(
    message = "Replaced with `Peripheral` builder function (not a CoroutineScope extension function).",
    replaceWith = ReplaceWith("Peripheral(advertisement, builderAction)"),
    level = DeprecationLevel.ERROR,
)
public actual fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral {
    advertisement as ScanResultAndroidAdvertisement
    return peripheral(advertisement._bluetoothDevice, builderAction)
}

@Deprecated(
    message = "Replaced with `Peripheral` builder function (not a CoroutineScope extension function).",
    replaceWith = ReplaceWith("Peripheral(bluetoothDevice, builderAction)"),
    level = DeprecationLevel.WARNING,
)
public fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    builderAction: PeripheralBuilderAction = {},
): Peripheral {
    val builder = PeripheralBuilder().apply(builderAction)
    val peripheral = BluetoothDeviceAndroidPeripheral(
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
    coroutineContext.job.invokeOnCompletion {
        peripheral.scope.cancel()
    }
    return peripheral
}

@Deprecated(
    message = "Replaced with `Peripheral` builder function (not a CoroutineScope extension function).",
    replaceWith = ReplaceWith("Peripheral(identifier, builderAction)"),
    level = DeprecationLevel.ERROR,
)
public fun CoroutineScope.peripheral(
    identifier: Identifier,
    builderAction: PeripheralBuilderAction = {},
): Peripheral {
    val bluetoothDevice = getBluetoothAdapter().getRemoteDevice(identifier)
    return peripheral(bluetoothDevice, builderAction)
}
