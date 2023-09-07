package com.juul.kable

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.CoroutineScope

public actual typealias Identifier = String

public actual fun String.toIdentifier(): Identifier {
    require(BluetoothAdapter.checkBluetoothAddress(this)) {
        "MAC Address has invalid format: $this"
    }
    return this
}

public actual fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral {
    advertisement as ScanResultAndroidAdvertisement
    return peripheral(advertisement.bluetoothDevice, builderAction)
}

public fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    builderAction: PeripheralBuilderAction = {},
): Peripheral {
    val builder = PeripheralBuilder()
    builder.builderAction()
    return BluetoothDeviceAndroidPeripheral(
        coroutineContext,
        bluetoothDevice,
        builder.autoConnect,
        builder.transport,
        builder.phy,
        builder.observationExceptionHandler,
        builder.onServicesDiscovered,
        builder.logging,
    )
}

public fun CoroutineScope.peripheral(
    identifier: Identifier,
    builderAction: PeripheralBuilderAction = {},
): Peripheral {
    val bluetoothDevice = getBluetoothAdapter().getRemoteDevice(identifier)
    return peripheral(bluetoothDevice, builderAction)
}
