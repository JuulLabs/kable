package com.juul.kable

import com.juul.kable.external.BluetoothDevice
import kotlinx.coroutines.CoroutineScope

public actual fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral {
    advertisement as BluetoothAdvertisingEventWebBluetoothAdvertisement
    return peripheral(advertisement.bluetoothDevice, builderAction)
}

internal fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    builderAction: PeripheralBuilderAction = {},
): WebBluetoothPeripheral {
    val builder = PeripheralBuilder()
    builder.builderAction()
    return BluetoothDeviceWebBluetoothPeripheral(
        coroutineContext,
        bluetoothDevice,
        builder.observationExceptionHandler,
        builder.onServicesDiscovered,
        builder.logging,
    )
}
