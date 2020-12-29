package com.juul.kable

import com.benasher44.uuid.Uuid
import platform.CoreBluetooth.CBAdvertisementDataServiceDataKey
import platform.CoreBluetooth.CBAdvertisementDataServiceUUIDsKey
import platform.CoreBluetooth.CBAdvertisementDataTxPowerLevelKey
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData
import platform.Foundation.NSNumber
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

    public actual val txPower: Int?
        get() = (data[CBAdvertisementDataTxPowerLevelKey] as? NSNumber)?.intValue

    actual val uuids: List<Uuid>
        get() = (data[CBAdvertisementDataServiceUUIDsKey] as? List<CBUUID>)?.map { it.toUuid() } ?: emptyList()

    public actual fun serviceData(uuid: Uuid): ByteArray? =
        serviceDataAsNSData(uuid)?.toByteArray()

    public fun serviceDataAsNSData(uuid: Uuid): NSData? =
        (data[CBAdvertisementDataServiceDataKey] as? Map<CBUUID, NSData>)?.get(uuid.toCBUUID())

    public actual fun manufacturerData(companyIdentifierCode: Short): ByteArray? =
        manufacturerData?.takeIf { (it.code == companyIdentifierCode) }?.let { it.data }

    public actual val manufacturerData: ManufacturerData?
        get() = manufacturerDataAsNSData?.toByteArray()?.toManufacturerData()

    public fun manufacturerDataAsNSData(companyIdentifierCode: Short): NSData? =
        manufacturerData(companyIdentifierCode)?.toNSData()

    public val manufacturerDataAsNSData: NSData?
        get() = data[CBAdvertisementDataServiceDataKey] as? NSData

    override fun toString(): String =
        "Advertisement(name=$name, cbPeripheral=$cbPeripheral, rssi=$rssi, txPower=$txPower)"
}

private fun ByteArray.firstTwoOctetsAsShort(): Short? =
    if (size >= 2) (this[0].toInt() + (this[1].toInt() shl 8)).toShort() else null

private fun ByteArray.toManufacturerData(): ManufacturerData? =
    firstTwoOctetsAsShort()?.let { code ->
        ManufacturerData(
            code,
            if (size > 2) slice(2..size).toByteArray() else byteArrayOf()
        )
    }
