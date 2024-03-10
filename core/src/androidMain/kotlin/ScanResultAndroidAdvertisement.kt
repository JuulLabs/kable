package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothDevice.BOND_BONDING
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Parcel
import android.os.ParcelUuid
import android.os.Parcelable
import com.benasher44.uuid.Uuid

internal class ScanResultAndroidAdvertisement(
    private val scanResult: ScanResult,
) : AndroidAdvertisement {

    internal val bluetoothDevice: BluetoothDevice
        get() = scanResult.device

    /** @see ScanRecord.getDeviceName */
    override val name: String?
        get() = scanResult.scanRecord?.deviceName

    /**
     * Retrieves the cached name from the local adapter. The local adapter caches the remote names during a device scan.
     *
     * @see BluetoothDevice.getName
     */
    override val peripheralName: String?
        get() = bluetoothDevice.name

    /**
     * Returns if the peripheral is connectable. Available on Android Oreo (API 26) and newer, on older versions of
     * Android, returns `null`.
     */
    override val isConnectable: Boolean?
        get() = if (VERSION.SDK_INT >= VERSION_CODES.O) scanResult.isConnectable else null

    override val address: String
        get() = bluetoothDevice.address

    override val identifier: Identifier
        get() = bluetoothDevice.address

    override val bondState: BondState
        get() = when (bluetoothDevice.bondState) {
            BOND_NONE -> BondState.None
            BOND_BONDING -> BondState.Bonding
            BOND_BONDED -> BondState.Bonded
            else -> error("Unknown bond state: ${bluetoothDevice.bondState}")
        }

    /** Returns raw bytes of the underlying scan record. */
    override val bytes: ByteArray?
        get() = scanResult.scanRecord?.bytes

    override val rssi: Int
        get() = scanResult.rssi

    override val txPower: Int?
        get() = scanResult.scanRecord?.txPowerLevel

    override val uuids: List<Uuid>
        get() = scanResult.scanRecord?.serviceUuids?.map { it.uuid } ?: emptyList()

    override fun serviceData(uuid: Uuid): ByteArray? =
        scanResult.scanRecord?.serviceData?.get(ParcelUuid(uuid))

    override fun manufacturerData(companyIdentifierCode: Int): ByteArray? =
        scanResult.scanRecord?.getManufacturerSpecificData(companyIdentifierCode)

    override val manufacturerData: ManufacturerData?
        get() = scanResult.scanRecord?.manufacturerSpecificData?.takeIf { it.size() > 0 }?.let {
            ManufacturerData(
                it.keyAt(0),
                it.valueAt(0),
            )
        }

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(scanResult, flags)
    }

    companion object CREATOR : Parcelable.Creator<AndroidAdvertisement?> {
        override fun createFromParcel(parcel: Parcel): AndroidAdvertisement =
            ScanResultAndroidAdvertisement(ScanResult.CREATOR.createFromParcel(parcel))

        override fun newArray(size: Int): Array<AndroidAdvertisement?> =
            arrayOfNulls(size)
    }

    override fun toString(): String =
        "Advertisement(address=$address, name=$name, rssi=$rssi, txPower=$txPower)"
}
