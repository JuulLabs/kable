package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothDevice.BOND_BONDING
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.ParcelUuid
import com.benasher44.uuid.Uuid

public enum class BondState {
    None,
    Bonding,
    Bonded,
}

public actual class Advertisement(
    private val scanResult: ScanResult,
) {

    internal val bluetoothDevice: BluetoothDevice
        get() = scanResult.device

    /** @see ScanRecord.getDeviceName */
    public actual val name: String?
        get() = scanResult.scanRecord?.deviceName

    /**
     * Retrieves the cached name from the local adapter. The local adapter caches the remote names during a device scan.
     *
     * @see BluetoothDevice.getName
     */
    public actual val peripheralName: String?
        get() = bluetoothDevice.name

    /**
     * Returns if the peripheral is connectable. Available on Android Oreo (API 26) and newer, on older versions of
     * Android, returns `null`.
     */
    public actual val isConnectable: Boolean?
        get() = if (VERSION.SDK_INT >= VERSION_CODES.O) scanResult.isConnectable else null

    public val address: String
        get() = bluetoothDevice.address

    public val bondState: BondState
        get() = when (bluetoothDevice.bondState) {
            BOND_NONE -> BondState.None
            BOND_BONDING -> BondState.Bonding
            BOND_BONDED -> BondState.Bonded
            else -> error("Unknown bond state: ${bluetoothDevice.bondState}")
        }

    /** Returns raw bytes of the underlying scan record. */
    public val bytes: ByteArray?
        get() = scanResult.scanRecord?.bytes

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
                it.valueAt(0),
            )
        }

    override fun toString(): String =
        "Advertisement(name=$name, bluetoothDevice=$bluetoothDevice, rssi=$rssi, txPower=$txPower)"
}
