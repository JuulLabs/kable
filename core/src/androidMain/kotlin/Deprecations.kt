package com.juul.kable

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.CoroutineScope

@Deprecated(
    message = "'writeObserveDescriptor' parameter is no longer used and is handled automatically by 'observe' function. 'writeObserveDescriptor' argument will be removed in a future release.",
    replaceWith = ReplaceWith("peripheral(advertisement)"),
    level = DeprecationLevel.ERROR,
)
public fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    writeObserveDescriptor: WriteNotificationDescriptor,
): Peripheral = throw UnsupportedOperationException()

@Deprecated(
    message = "'writeObserveDescriptor' parameter is no longer used and is handled automatically by 'observe' function. 'writeObserveDescriptor' argument will be removed in a future release.",
    replaceWith = ReplaceWith("peripheral(advertisement)"),
    level = DeprecationLevel.ERROR,
)
public fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    writeObserveDescriptor: WriteNotificationDescriptor,
): Peripheral = throw UnsupportedOperationException()

/**
 * @param transport preferred transport for GATT connections to remote dual-mode devices.
 * @param phy preferred PHY for connections to remote LE device.
 */
@Deprecated(
    message = "Use builder lambda",
    replaceWith = ReplaceWith("""peripheral(advertisement) {
    transport = Transport.Le
    phy = Phy.Le1M
}"""),
)
public fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    transport: Transport = Transport.Le,
    phy: Phy = Phy.Le1M,
): Peripheral = peripheral(advertisement) {
    this.transport = transport
    this.phy = phy
}

/**
 * @param transport preferred transport for GATT connections to remote dual-mode devices.
 * @param phy preferred PHY for connections to remote LE device.
 */
@Deprecated(
    message = "Use builder lambda",
    replaceWith = ReplaceWith("""peripheral(bluetoothDevice) {
    transport = Transport.Le
    phy = Phy.Le1M
}"""),
)
public fun CoroutineScope.peripheral(
    bluetoothDevice: BluetoothDevice,
    transport: Transport = Transport.Le,
    phy: Phy = Phy.Le1M,
): Peripheral = peripheral(bluetoothDevice) {
    this.transport = transport
    this.phy = phy
}
