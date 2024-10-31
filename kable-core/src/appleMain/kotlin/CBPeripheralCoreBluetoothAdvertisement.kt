package com.juul.kable

import platform.CoreBluetooth.CBAdvertisementDataIsConnectable
import platform.CoreBluetooth.CBAdvertisementDataLocalNameKey
import platform.CoreBluetooth.CBAdvertisementDataManufacturerDataKey
import platform.CoreBluetooth.CBAdvertisementDataServiceDataKey
import platform.CoreBluetooth.CBAdvertisementDataServiceUUIDsKey
import platform.CoreBluetooth.CBAdvertisementDataTxPowerLevelKey
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData
import platform.Foundation.NSNumber
import kotlin.experimental.ExperimentalNativeApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class CBPeripheralCoreBluetoothAdvertisement(
    override val rssi: Int,
    private val data: Map<String, Any>,
    internal val cbPeripheral: CBPeripheral,
) : PlatformAdvertisement {

    override val identifier: Identifier
        get() = cbPeripheral.identifier.toUuid()

    override val name: String?
        get() = data[CBAdvertisementDataLocalNameKey] as? String

    /**
     * The [peripheralName] property may contain either advertising, or GAP name, dependent on various conditions:
     *
     * | Condition(s)                               | `gapName` value                                      |
     * |--------------------------------------------|------------------------------------------------------|
     * | Initial peripheral discovery (active scan) | `advertisementData(CBAdvertisementDataLocalNameKey)` |
     * | Connected to peripheral                    | GAP name                                             |
     * | Subsequent discovery (disconnected)        | GAP name (from cache)                                |
     *
     * https://developer.apple.com/forums/thread/72343
     */
    override val peripheralName: String?
        get() = cbPeripheral.name

    /** https://developer.apple.com/documentation/corebluetooth/cbadvertisementdataisconnectable */
    override val isConnectable: Boolean?
        get() = (data[CBAdvertisementDataIsConnectable] as? NSNumber)?.boolValue

    override val txPower: Int?
        get() = (data[CBAdvertisementDataTxPowerLevelKey] as? NSNumber)?.intValue

    override val uuids: List<Uuid>
        get() = (data[CBAdvertisementDataServiceUUIDsKey] as? List<CBUUID>)?.map { it.toUuid() } ?: emptyList()

    override fun serviceData(uuid: Uuid): ByteArray? =
        serviceDataAsNSData(uuid)?.toByteArray()

    override fun serviceDataAsNSData(uuid: Uuid): NSData? =
        (data[CBAdvertisementDataServiceDataKey] as? Map<CBUUID, NSData>)?.get(uuid.toCBUUID())

    override fun manufacturerData(companyIdentifierCode: Int): ByteArray? =
        manufacturerData?.takeIf { (it.code == companyIdentifierCode) }?.data

    override val manufacturerData: ManufacturerData?
        get() = manufacturerDataAsNSData?.toManufacturerData()

    override fun manufacturerDataAsNSData(companyIdentifierCode: Int): NSData? =
        manufacturerData(companyIdentifierCode)?.toNSData()

    override val manufacturerDataAsNSData: NSData?
        get() = data[CBAdvertisementDataManufacturerDataKey] as? NSData

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CBPeripheralCoreBluetoothAdvertisement) return false
        if (identifier != other.identifier) return false
        if (rssi != other.rssi) return false
        return data == other.data
    }

    override fun hashCode(): Int {
        var result = rssi.hashCode()
        result = 31 * result + data.hashCode()
        result = 31 * result + identifier.hashCode()
        return result
    }

    override fun toString(): String =
        "Advertisement(identifier=$identifier, name=$name, rssi=$rssi, txPower=$txPower)"
}

internal fun NSData.toManufacturerData(): ManufacturerData? = toByteArray().toManufacturerData()

@OptIn(ExperimentalNativeApi::class)
private fun ByteArray.toManufacturerData(): ManufacturerData? =
    takeIf { size >= 2 }?.getShortAt(0)?.let { code ->
        ManufacturerData(
            code.toInt(),
            if (size > 2) slice(2 until size).toByteArray() else byteArrayOf(),
        )
    }
