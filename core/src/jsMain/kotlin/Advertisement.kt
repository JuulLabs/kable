package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.BluetoothAdvertisingEvent
import com.juul.kable.external.BluetoothDevice
import com.juul.kable.external.iterable
import org.khronos.webgl.DataView

public actual class Advertisement internal constructor(
    private val advertisement: BluetoothAdvertisingEvent,
) {
    internal val bluetoothDevice: BluetoothDevice
        get() = advertisement.device

    public actual val name: String?
        get() = bluetoothDevice.name

    /** On JavaScript, this property is the same as [name]. */
    public actual val peripheralName: String?
        get() = name

    public actual val rssi: Int
        get() = advertisement.rssi ?: Int.MIN_VALUE

    public actual val txPower: Int?
        get() = advertisement.txPower

    public actual val uuids: List<Uuid>
        get() = advertisement.uuids.map { it.toUuid() }

    public actual fun serviceData(uuid: Uuid): ByteArray? =
        serviceDataAsDataView(uuid)?.buffer?.toByteArray()

    public actual fun manufacturerData(companyIdentifierCode: Int): ByteArray? =
        manufacturerDataAsDataView(companyIdentifierCode)?.buffer?.toByteArray()

    public fun serviceDataAsDataView(uuid: Uuid): DataView? =
        advertisement.serviceData.asDynamic().get(uuid.toString()) as? DataView

    public fun manufacturerDataAsDataView(companyIdentifierCode: Int): DataView? =
        advertisement.manufacturerData.asDynamic().get(companyIdentifierCode.toString()) as? DataView

    public actual val manufacturerData: ManufacturerData?
        get() = advertisement.manufacturerData.entries().iterable().firstOrNull()?.let { entry ->
            ManufacturerData(
                entry[0] as Int,
                (entry[1] as DataView).buffer.toByteArray(),
            )
        }

    override fun toString(): String =
        "Advertisement(name=$name, bluetoothDevice=$bluetoothDevice, rssi=$rssi, txPower=$txPower)"
}
