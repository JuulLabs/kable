package com.juul.kable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * Snapshot of the data carried by an [Advertisement], used as the serial (wire) representation of
 * [Advertisement] and [PlatformAdvertisement].
 *
 * [identifier] is retained as its [String] representation ([Identifier] is a platform specific
 * type). Serialized advertisements are only meant to be deserialized on the same platform (an
 * [Identifier] is not portable across platforms).
 */
@Serializable
@SerialName("com.juul.kable.Advertisement")
internal class AdvertisementCapture(
    val name: String?,
    val peripheralName: String?,
    val identifier: String,
    val isConnectable: Boolean?,
    val rssi: Int,
    val txPower: Int?,
    val uuids: List<Uuid>,
    val serviceData: Map<Uuid, ByteArray>,
    val manufacturerData: Map<Int, ByteArray>,
    /** Raw bytes of the underlying scan record. Only available on Android. */
    val bytes: ByteArray? = null,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AdvertisementCapture) return false
        return name == other.name &&
            peripheralName == other.peripheralName &&
            identifier == other.identifier &&
            isConnectable == other.isConnectable &&
            rssi == other.rssi &&
            txPower == other.txPower &&
            uuids == other.uuids &&
            serviceData.contentEquals(other.serviceData) &&
            manufacturerData.contentEquals(other.manufacturerData) &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + peripheralName.hashCode()
        result = 31 * result + identifier.hashCode()
        result = 31 * result + isConnectable.hashCode()
        result = 31 * result + rssi
        result = 31 * result + txPower.hashCode()
        result = 31 * result + uuids.hashCode()
        result = 31 * result + serviceData.contentHashCode()
        result = 31 * result + manufacturerData.contentHashCode()
        result = 31 * result + (bytes?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "Advertisement(identifier=$identifier, name=$name, rssi=$rssi, txPower=$txPower)"
}

/** Captures the data of an [Advertisement] that is visible via the common [Advertisement] API. */
internal fun Advertisement.captureCommon(): AdvertisementCapture = AdvertisementCapture(
    name = name,
    peripheralName = peripheralName,
    identifier = identifier.toString(),
    isConnectable = isConnectable,
    rssi = rssi,
    txPower = txPower,
    uuids = uuids,
    // Common `Advertisement` API does not provide enumeration of service data.
    serviceData = emptyMap(),
    manufacturerData = manufacturerData?.let { mapOf(it.code to it.data) }.orEmpty(),
)

/** Captures the data of this [PlatformAdvertisement] (including platform specific data). */
internal expect fun PlatformAdvertisement.capture(): AdvertisementCapture

/** Restores a previously [captured][capture] advertisement as a [PlatformAdvertisement]. */
internal expect fun AdvertisementCapture.restore(): PlatformAdvertisement

private fun <K> Map<K, ByteArray>.contentEquals(other: Map<K, ByteArray>): Boolean =
    size == other.size && all { (key, value) -> other[key]?.contentEquals(value) == true }

// Sum of entry hashes (order independent, in line with `Map.hashCode` contract).
private fun Map<*, ByteArray>.contentHashCode(): Int =
    entries.sumOf { (key, value) -> key.hashCode() xor value.contentHashCode() }
