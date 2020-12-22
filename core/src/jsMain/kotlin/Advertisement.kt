package com.juul.kable

import com.juul.kable.external.BluetoothDevice
import org.khronos.webgl.DataView

public actual class Advertisement internal constructor(
    internal val bluetoothDevice: BluetoothDevice,
    public val uuids: Array<String>,
    public actual val rssi: Int,
    internal val manufacturerData: Any?,
    internal val serviceData: Any?,
) {

    public actual val name: String?
        get() = bluetoothDevice.name

    public fun getServiceData(key: String): DataView? =
        serviceData.asDynamic().get(key) as? DataView

    public fun getManufacturerData(key: String): DataView? =
        manufacturerData.asDynamic().get(key) as? DataView
}
