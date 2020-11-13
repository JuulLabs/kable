package com.juul.kable

import platform.CoreBluetooth.CBPeripheral
import platform.Foundation.NSUUID

public actual data class Advertisement(
    public actual val rssi: Int,
    val data: Map<String, Any>,
    internal val cbPeripheral: CBPeripheral,
) {

    val identifier: NSUUID
        get() = cbPeripheral.identifier

    public actual val name: String?
        get() = cbPeripheral.name
}
