package com.juul.kable

import com.juul.kable.external.BluetoothDevice

public actual class Advertisement internal constructor(
        internal val bluetoothDevice: BluetoothDevice,
        public actual val rssi: Int,
) {

    public actual val name: String?
        get() = bluetoothDevice.name
}
