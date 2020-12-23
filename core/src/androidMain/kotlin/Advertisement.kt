package com.juul.kable

import android.bluetooth.BluetoothDevice
import com.benasher44.uuid.Uuid

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

    public actual val txPower: Int?
        get() = TODO("Not yet implemented")

    public actual val uuids: List<Uuid>
        get() = TODO("Not yet implemented")

    public actual fun serviceData(uuid: Uuid): ByteArray? {
        TODO("Not yet implemented")
    }

    public actual fun manufacturerData(companyIdentifierCode: Short): ByteArray? {
        TODO("Not yet implemented")
    }
}
