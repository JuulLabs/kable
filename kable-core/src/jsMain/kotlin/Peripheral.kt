package com.juul.kable

import com.juul.kable.external.BluetoothDevice
import kotlinx.coroutines.CoroutineScope

/**
 * This function will soon be deprecated in favor of suspend version of function (with
 * [CoroutineScope] as parameter).
 *
 * See https://github.com/JuulLabs/kable/issues/286 for more details.
 */
@ObsoleteKableApi
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
): WebBluetoothPeripheral = peripheral(bluetoothDevice, PeripheralBuilder().apply(builderAction))

internal fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    builder: PeripheralBuilder,
): WebBluetoothPeripheral = builder.build(bluetoothDevice, this)
