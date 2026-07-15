package com.juul.kable

import org.khronos.webgl.DataView
import org.khronos.webgl.toInt8Array
import kotlin.uuid.Uuid

/**
 * [PlatformAdvertisement] restored from previously captured advertisement data (i.e. produced by
 * deserializing a serialized [PlatformAdvertisement]).
 *
 * All properties reflect the advertisement at the time it was captured.
 *
 * A [Peripheral] cannot be created from a restored advertisement (Web Bluetooth requires a
 * `BluetoothDevice`, which cannot be synchronously retrieved); use the `Peripheral(Identifier)`
 * builder function instead.
 */
internal class CapturedWebBluetoothAdvertisement(
    internal val capture: AdvertisementCapture,
) : PlatformAdvertisement {

    override val identifier: Identifier
        get() = capture.identifier

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

    override fun serviceDataAsDataView(uuid: Uuid): DataView? =
        capture.serviceData[uuid]?.toDataView()

    override fun manufacturerData(companyIdentifierCode: Int): ByteArray? =
        capture.manufacturerData[companyIdentifierCode]

    override fun manufacturerDataAsDataView(companyIdentifierCode: Int): DataView? =
        capture.manufacturerData[companyIdentifierCode]?.toDataView()

    override val manufacturerData: ManufacturerData?
        get() = capture.manufacturerData.entries.firstOrNull()
            ?.let { (code, data) -> ManufacturerData(code, data) }

    override fun equals(other: Any?): Boolean =
        other is CapturedWebBluetoothAdvertisement && capture == other.capture

    override fun hashCode(): Int = capture.hashCode()

    override fun toString(): String = capture.toString()
}

private fun ByteArray.toDataView(): DataView = DataView(toInt8Array().buffer)
