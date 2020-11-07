package com.juul.kable

import android.bluetooth.BluetoothDevice

public actual class Advertisement(
    public actual val rssi: Int,
    internal val bluetoothDevice: BluetoothDevice,
) {

    public actual val name: String?
        get() = bluetoothDevice.name

    public val address: String
        get() = bluetoothDevice.address

    override fun toString(): String =
        "Advertisement(name=$name, rssi=$rssi, bluetoothDevice=$bluetoothDevice)"
}
