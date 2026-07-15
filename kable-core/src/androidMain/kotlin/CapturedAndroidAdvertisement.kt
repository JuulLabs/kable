package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.os.Parcel
import com.juul.kable.PlatformAdvertisement.BondState
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlin.uuid.Uuid

/**
 * [PlatformAdvertisement] restored from previously captured advertisement data (i.e. produced by
 * deserializing a serialized [PlatformAdvertisement]).
 *
 * Most properties reflect the advertisement at the time it was captured, with the following
 * exceptions (which are retrieved, on demand, from the local Bluetooth adapter):
 * - [bondState]
 * - [bluetoothDevice]
 */
@Parcelize
@TypeParceler<AdvertisementCapture, AdvertisementCaptureParceler>()
internal class CapturedAndroidAdvertisement(
    internal val capture: AdvertisementCapture,
) : PlatformAdvertisement {

    /** @throws IllegalStateException If bluetooth is not supported. */
    @InternalKableApi
    override val bluetoothDevice: BluetoothDevice
        get() = getBluetoothAdapter().getRemoteDevice(address.uppercase())

    override val name: String?
        get() = capture.name

    override val peripheralName: String?
        get() = capture.peripheralName

    override val address: String
        get() = capture.identifier

    override val identifier: Identifier
        get() = capture.identifier

    /** @throws IllegalStateException If bluetooth is not supported. */
    override val bondState: BondState
        get() = bluetoothDevice.toBondState()

    override val isConnectable: Boolean?
        get() = capture.isConnectable

    override val bytes: ByteArray?
        get() = capture.bytes

    override val rssi: Int
        get() = capture.rssi

    override val txPower: Int?
        get() = capture.txPower

    override val uuids: List<Uuid>
        get() = capture.uuids

    override fun serviceData(uuid: Uuid): ByteArray? = capture.serviceData[uuid]

    override fun manufacturerData(companyIdentifierCode: Int): ByteArray? =
        capture.manufacturerData[companyIdentifierCode]

    override val manufacturerData: ManufacturerData?
        get() = capture.manufacturerData.entries.firstOrNull()
            ?.let { (code, data) -> ManufacturerData(code, data) }

    override fun equals(other: Any?): Boolean =
        other is CapturedAndroidAdvertisement && capture == other.capture

    override fun hashCode(): Int = capture.hashCode()

    override fun toString(): String = capture.toString()
}

internal object AdvertisementCaptureParceler : Parceler<AdvertisementCapture> {

    override fun create(parcel: Parcel): AdvertisementCapture = AdvertisementCapture(
        name = parcel.readString(),
        peripheralName = parcel.readString(),
        identifier = parcel.readString()!!,
        isConnectable = parcel.readValue(Boolean::class.java.classLoader) as Boolean?,
        rssi = parcel.readInt(),
        txPower = parcel.readValue(Int::class.java.classLoader) as Int?,
        uuids = List(parcel.readInt()) { Uuid.parse(parcel.readString()!!) },
        serviceData = List(parcel.readInt()) {
            Uuid.parse(parcel.readString()!!) to parcel.createByteArray()!!
        }.toMap(),
        manufacturerData = List(parcel.readInt()) {
            parcel.readInt() to parcel.createByteArray()!!
        }.toMap(),
        bytes = parcel.createByteArray(),
    )

    override fun AdvertisementCapture.write(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(peripheralName)
        parcel.writeString(identifier)
        parcel.writeValue(isConnectable)
        parcel.writeInt(rssi)
        parcel.writeValue(txPower)
        parcel.writeInt(uuids.size)
        uuids.forEach { uuid -> parcel.writeString(uuid.toString()) }
        parcel.writeInt(serviceData.size)
        serviceData.forEach { (uuid, data) ->
            parcel.writeString(uuid.toString())
            parcel.writeByteArray(data)
        }
        parcel.writeInt(manufacturerData.size)
        manufacturerData.forEach { (code, data) ->
            parcel.writeInt(code)
            parcel.writeByteArray(data)
        }
        parcel.writeByteArray(bytes)
    }
}
