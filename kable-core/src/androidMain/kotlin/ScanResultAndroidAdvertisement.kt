package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.ParcelUuid
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import com.juul.kable.PlatformAdvertisement.BondState
import kotlinx.parcelize.Parcelize
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

@Parcelize
internal class ScanResultAndroidAdvertisement(
    private val scanResult: ScanResult,
) : PlatformAdvertisement {

    @InternalKableApi
    override val bluetoothDevice: BluetoothDevice
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
        get() = bluetoothDevice.toBondState()

    /** Returns raw bytes of the underlying scan record. */
    override val bytes: ByteArray?
        get() = scanResult.scanRecord?.bytes

    override val rssi: Int
        get() = scanResult.rssi

    override val txPower: Int?
        get() = scanResult.scanRecord?.txPowerLevel

    override val uuids: List<Uuid>
        get() = scanResult.scanRecord?.serviceUuids?.map { it.uuid.toKotlinUuid() } ?: emptyList()

    internal val serviceData: Map<ParcelUuid, ByteArray>?
        get() = scanResult.scanRecord?.serviceData

    override fun serviceData(uuid: Uuid): ByteArray? =
        scanResult.scanRecord?.serviceData?.get(ParcelUuid(uuid.toJavaUuid()))

    override fun manufacturerData(companyIdentifierCode: Int): ByteArray? =
        scanResult.scanRecord?.getManufacturerSpecificData(companyIdentifierCode)

    override val manufacturerData: ManufacturerData?
        get() = scanResult.scanRecord?.manufacturerSpecificData?.takeIf { it.isNotEmpty() }?.let {
            ManufacturerData(
                it.keyAt(0),
                it.valueAt(0),
            )
        }

    internal fun capture(): AdvertisementCapture = AdvertisementCapture(
        name = name,
        peripheralName = peripheralName,
        identifier = address,
        isConnectable = isConnectable,
        rssi = rssi,
        txPower = txPower,
        uuids = uuids,
        serviceData = serviceData
            ?.entries
            ?.associate { (uuid, data) -> uuid.uuid.toKotlinUuid() to data }
            .orEmpty(),
        manufacturerData = scanResult.scanRecord?.manufacturerSpecificData?.toMap().orEmpty(),
        bytes = bytes,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScanResultAndroidAdvertisement) return false
        return scanResult == other.scanResult
    }

    override fun hashCode(): Int = scanResult.hashCode()

    override fun toString(): String =
        "Advertisement(address=$address, name=$name, rssi=$rssi, txPower=$txPower)"
}

private fun SparseArray<ByteArray>.toMap(): Map<Int, ByteArray> =
    buildMap { this@toMap.forEach { key, value -> put(key, value) } }
