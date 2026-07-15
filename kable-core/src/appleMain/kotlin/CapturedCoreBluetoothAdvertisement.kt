package com.juul.kable

import platform.CoreBluetooth.CBPeripheral
import platform.Foundation.NSData
import kotlin.uuid.Uuid

/**
 * [PlatformAdvertisement] restored from previously captured advertisement data (i.e. produced by
 * deserializing a serialized [PlatformAdvertisement]).
 *
 * All properties reflect the advertisement at the time it was captured, except [cbPeripheral],
 * which is retrieved (on demand) from the system.
 */
internal class CapturedCoreBluetoothAdvertisement(
    internal val capture: AdvertisementCapture,
) : PlatformAdvertisement {

    override val identifier: Identifier = Uuid.parse(capture.identifier)

    /** @throws NoSuchElementException If the system has no knowledge of a peripheral with [identifier]. */
    @InternalKableApi
    override val cbPeripheral: CBPeripheral
        get() = CentralManager.Default.retrievePeripheral(identifier)
            ?: throw NoSuchElementException("Peripheral with UUID $identifier not found")

    override val name: String?
        get() = capture.name

    override val peripheralName: String?
        get() = capture.peripheralName

    override val isConnectable: Boolean?
        get() = capture.isConnectable

    override val rssi: Int
        get() = capture.rssi

    override val txPower: Int?
        get() = capture.txPower

    override val uuids: List<Uuid>
        get() = capture.uuids

    override fun serviceData(uuid: Uuid): ByteArray? = capture.serviceData[uuid]

    override fun serviceDataAsNSData(uuid: Uuid): NSData? = capture.serviceData[uuid]?.toNSData()

    override fun manufacturerData(companyIdentifierCode: Int): ByteArray? =
        capture.manufacturerData[companyIdentifierCode]

    override fun manufacturerDataAsNSData(companyIdentifierCode: Int): NSData? =
        manufacturerData(companyIdentifierCode)?.toNSData()

    override val manufacturerData: ManufacturerData?
        get() = capture.manufacturerData.entries.firstOrNull()
            ?.let { (code, data) -> ManufacturerData(code, data) }

    override val manufacturerDataAsNSData: NSData?
        get() = capture.manufacturerData.entries.firstOrNull()
            ?.let { (code, data) ->
                // Reconstructs the Manufacturer Specific Data layout (two octet little-endian
                // Company Identifier Code, followed by the manufacturer data).
                byteArrayOf(
                    (code and 0xFF).toByte(),
                    ((code shr 8) and 0xFF).toByte(),
                ).plus(data).toNSData()
            }

    override fun equals(other: Any?): Boolean =
        other is CapturedCoreBluetoothAdvertisement && capture == other.capture

    override fun hashCode(): Int = capture.hashCode()

    override fun toString(): String = capture.toString()
}
