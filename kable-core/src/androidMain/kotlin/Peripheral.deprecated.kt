package com.juul.kable

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job

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
        peripheral.cancel()
    }
    return peripheral
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
