package com.juul.kable

import com.juul.kable.external.BluetoothAdvertisingEvent
import com.juul.kable.external.BluetoothDevice
import js.array.component1
import js.array.component2
import js.iterable.iterator
import org.khronos.webgl.DataView
import kotlin.js.toInt
import kotlin.js.toJsNumber
import kotlin.js.toJsString
import kotlin.js.toList
import kotlin.uuid.Uuid

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
        get() = advertisement.uuids.toList().map { it.toString().toUuid() }

    override fun serviceData(uuid: Uuid): ByteArray? =
        serviceDataAsDataView(uuid)?.buffer?.toByteArray()

    override fun manufacturerData(companyIdentifierCode: Int): ByteArray? =
        manufacturerDataAsDataView(companyIdentifierCode)?.buffer?.toByteArray()

    override fun serviceDataAsDataView(uuid: Uuid): DataView? =
        advertisement.serviceData.get(uuid.toString().toJsString())

    override fun manufacturerDataAsDataView(companyIdentifierCode: Int): DataView? =
        advertisement.manufacturerData.get(companyIdentifierCode.toJsNumber())

    override val manufacturerData: ManufacturerData?
        get() = Iterable { advertisement.manufacturerData.entries().iterator() }.firstOrNull()?.let { (key, value) ->
            ManufacturerData(key.toInt(), value.buffer.toByteArray())
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as BluetoothAdvertisingEventWebBluetoothAdvertisement
        return advertisement == other.advertisement
    }

    override fun hashCode(): Int = advertisement.hashCode()

    override fun toString(): String =
        "Advertisement(identifier=$identifier, name=$name, rssi=$rssi, txPower=$txPower)"
}
