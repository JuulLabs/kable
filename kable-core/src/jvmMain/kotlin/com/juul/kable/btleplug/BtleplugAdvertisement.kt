package com.juul.kable.btleplug

import com.juul.kable.Identifier
import com.juul.kable.ManufacturerData
import com.juul.kable.PlatformAdvertisement
import com.juul.kable.btleplug.ffi.PeripheralProperties
import kotlinx.io.bytestring.ByteString
import kotlin.uuid.Uuid

internal data class BtleplugAdvertisement(
    private val manufacturerDataMap: Map<UShort, ByteString>,
    internal val serviceData: Map<Uuid, ByteString>,
    override val name: String?,
    override val peripheralName: String?,
    override val identifier: Identifier,
    override val rssi: Int,
    override val txPower: Int?,
    override val uuids: List<Uuid>,
) : PlatformAdvertisement {

    constructor(properties: PeripheralProperties) : this(
        manufacturerDataMap = properties.manufacturerData.asSequence()
            .associate { (key, value) -> key to ByteString(value) },
        serviceData = properties.serviceData.asSequence()
            .associate { (key, value) -> Uuid.parse(key) to ByteString(value) },
        name = properties.localName,
        peripheralName = properties.localName,
        identifier = Identifier(properties.id),
        rssi = properties.rssi?.toInt() ?: Int.MIN_VALUE,
        txPower = properties.txPowerLevel?.toInt(),
        uuids = properties.services.map(Uuid::parse),
    )

    override val isConnectable: Boolean?
        get() = null // Doesn't exist in btleplug

    // TODO: Figure out why Btleplug exposes this as a map.
    override val manufacturerData: ManufacturerData?
        get() = manufacturerDataMap.asSequence()
            .singleOrNull()
            ?.let { (key, value) -> ManufacturerData(key.toInt(), value.toByteArray()) }

    override fun serviceData(uuid: Uuid): ByteArray? =
        serviceData[uuid]?.toByteArray()

    override fun manufacturerData(companyIdentifierCode: Int): ByteArray? =
        manufacturerDataMap[companyIdentifierCode.toUShort()]?.toByteArray()
}
