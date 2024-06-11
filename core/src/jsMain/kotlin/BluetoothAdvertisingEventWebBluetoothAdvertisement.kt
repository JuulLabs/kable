package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.BluetoothAdvertisingEvent
import com.juul.kable.external.BluetoothDevice
import com.juul.kable.external.iterable
import org.khronos.webgl.DataView

internal class BluetoothAdvertisingEventWebBluetoothAdvertisement(
    private val advertisement: BluetoothAdvertisingEvent,
) : PlatformAdvertisement {

    internal val bluetoothDevice: BluetoothDevice
        get() = advertisement.device

    override val identifier: Identifier
        get() = advertisement.device.id

    override val name: String?
        get() = advertisement.name

    override val peripheralName: String?
        get() = advertisement.device.name

    /** Property is unavailable on JavaScript. Always returns `null`. */
    override val isConnectable: Boolean? = null

    override val rssi: Int
        get() = advertisement.rssi ?: Int.MIN_VALUE

    override val txPower: Int?
        get() = advertisement.txPower

    override val uuids: List<Uuid>
        get() = advertisement.uuids.map { it.toUuid() }

    override fun serviceData(uuid: Uuid): ByteArray? =
        serviceDataAsDataView(uuid)?.buffer?.toByteArray()

    override fun manufacturerData(companyIdentifierCode: Int): ByteArray? =
        manufacturerDataAsDataView(companyIdentifierCode)?.buffer?.toByteArray()

    override fun serviceDataAsDataView(uuid: Uuid): DataView? =
        advertisement.serviceData.asDynamic().get(uuid.toString()) as? DataView

    override fun manufacturerDataAsDataView(companyIdentifierCode: Int): DataView? =
        advertisement.manufacturerData.asDynamic().get(companyIdentifierCode.toString()) as? DataView

    override val manufacturerData: ManufacturerData?
        get() = advertisement.manufacturerData.entries().iterable().firstOrNull()?.let { entry ->
            ManufacturerData(
                entry[0] as Int,
                (entry[1] as DataView).buffer.toByteArray(),
            )
        }

    override fun toString(): String =
        "Advertisement(identifier=$identifier, name=$name, rssi=$rssi, txPower=$txPower)"
}
