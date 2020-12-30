package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.ParcelUuid
import com.benasher44.uuid.Uuid

public actual class Advertisement(
    private val scanResult: ScanResult,
) {

    internal val bluetoothDevice: BluetoothDevice
        get() = scanResult.device

    public actual val name: String?
        get() = bluetoothDevice.name

    public val address: String
        get() = bluetoothDevice.address

    public actual val rssi: Int
        get() = scanResult.rssi

    public actual val txPower: Int?
        get() = scanResult.scanRecord?.txPowerLevel

    public actual val uuids: List<Uuid>
        get() = scanResult.scanRecord?.serviceUuids?.map { it.uuid } ?: emptyList()

    public actual fun serviceData(uuid: Uuid): ByteArray? =
        scanResult.scanRecord?.serviceData?.get(ParcelUuid(uuid))

    public actual fun manufacturerData(companyIdentifierCode: Int): ByteArray? =
        scanResult.scanRecord?.getManufacturerSpecificData(companyIdentifierCode)

    public actual val manufacturerData: ManufacturerData?
        get() = scanResult.scanRecord?.manufacturerSpecificData?.takeIf { it.size() > 0 }?.let {
            ManufacturerData(
                it.keyAt(0),
                it.valueAt(0)
            )
        }

    override fun toString(): String =
        "Advertisement(name=$name, bluetoothDevice=$bluetoothDevice, rssi=$rssi, txPower=$txPower)"
}
