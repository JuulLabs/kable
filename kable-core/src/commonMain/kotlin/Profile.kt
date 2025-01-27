@file:Suppress("ktlint:standard:no-multi-spaces")

package com.juul.kable

import com.juul.kable.Characteristic.Properties
import com.juul.kable.WriteType.WithResponse
import com.juul.kable.WriteType.WithoutResponse
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

public interface Service {
    public val serviceUuid: Uuid
}

internal val Broadcast = Properties(1 shl 0)            // 0x01
internal val Read = Properties(1 shl 1)                 // 0x02
internal val WriteWithoutResponse = Properties(1 shl 2) // 0x04
internal val Write = Properties(1 shl 3)                // 0x08
internal val Notify = Properties(1 shl 4)               // 0x10
internal val Indicate = Properties(1 shl 5)             // 0x20
internal val SignedWrite = Properties(1 shl 6)          // 0x40
internal val ExtendedProperties = Properties(1 shl 7)   // 0x80

public val Properties.broadcast: Boolean
    get() = value and Broadcast.value != 0

public val Properties.read: Boolean
    get() = value and Read.value != 0

public val Properties.writeWithoutResponse: Boolean
    get() = value and WriteWithoutResponse.value != 0

public val Properties.write: Boolean
    get() = value and Write.value != 0

public val Properties.notify: Boolean
    get() = value and Notify.value != 0

public val Properties.indicate: Boolean
    get() = value and Indicate.value != 0

public val Properties.signedWrite: Boolean
    get() = value and SignedWrite.value != 0

public val Properties.extendedProperties: Boolean
    get() = value and ExtendedProperties.value != 0

internal val WriteType.properties: Properties
    get() = when (this) {
        WithResponse -> Write
        WithoutResponse -> WriteWithoutResponse
    }

public interface Characteristic {
    public val serviceUuid: Uuid
    public val characteristicUuid: Uuid

    @JvmInline
    public value class Properties internal constructor(public val value: Int) {
        internal infix fun or(other: Properties): Properties = Properties(value or other.value)
        internal infix fun and(other: Properties): Properties = Properties(value and other.value)
        override fun toString(): String =
            mutableListOf<String>().apply {
                if (broadcast) add("broadcast")
                if (read) add("read")
                if (writeWithoutResponse) add("writeWithoutResponse")
                if (write) add("write")
                if (notify) add("notify")
                if (indicate) add("indicate")
                if (signedWrite) add("signedWrite")
            }.joinToString()
    }
}

public interface Descriptor {
    public val serviceUuid: Uuid
    public val characteristicUuid: Uuid
    public val descriptorUuid: Uuid
}

internal expect class PlatformService
internal expect class PlatformCharacteristic
internal expect class PlatformDescriptor

public interface DiscoveredService : Service {
    public val characteristics: List<DiscoveredCharacteristic>
}

public interface DiscoveredCharacteristic : Characteristic {
    public val descriptors: List<DiscoveredDescriptor>
    public val properties: Properties
}

public interface DiscoveredDescriptor : Descriptor

/** Wrapper around platform specific Bluetooth LE service. Holds a strong reference to underlying service. */
internal expect class PlatformDiscoveredService : DiscoveredService {
    val service: PlatformService
    override val serviceUuid: Uuid
    override val characteristics: List<PlatformDiscoveredCharacteristic>
}

/** Wrapper around platform specific Bluetooth LE characteristic. Holds a strong reference to underlying characteristic. */
internal expect class PlatformDiscoveredCharacteristic : DiscoveredCharacteristic {
    val characteristic: PlatformCharacteristic
    override val serviceUuid: Uuid
    override val characteristicUuid: Uuid
    override val descriptors: List<PlatformDiscoveredDescriptor>
    override val properties: Properties
}

/** Wrapper around platform specific Bluetooth LE descriptor. Holds a strong reference to underlying descriptor. */
internal expect class PlatformDiscoveredDescriptor : DiscoveredDescriptor {
    val descriptor: PlatformDescriptor
    override val serviceUuid: Uuid
    override val characteristicUuid: Uuid
    override val descriptorUuid: Uuid
}

public data class LazyCharacteristic internal constructor(
    override val serviceUuid: Uuid,
    override val characteristicUuid: Uuid,
) : Characteristic

public data class LazyDescriptor(
    public override val serviceUuid: Uuid,
    public override val characteristicUuid: Uuid,
    public override val descriptorUuid: Uuid,
) : Descriptor

@Deprecated(
    """
    Use `characteristicOf` that accepts `Uuid` arguments.
    Example: `characteristicOf(Uuid.service("battery_service"), Uuid.characteristic("battery_level"))`,
    """,
    replaceWith = ReplaceWith("characteristicOf(Uuid.parse(service), Uuid.parse(characteristic))"),
)
public fun characteristicOf(
    service: String,
    characteristic: String,
): Characteristic = LazyCharacteristic(
    serviceUuid = Uuid.parse(service),
    characteristicUuid = Uuid.parse(characteristic),
)

public fun characteristicOf(service: Uuid, characteristic: Uuid): Characteristic =
    LazyCharacteristic(service, characteristic)

@Deprecated(
    "Use `descriptorOf` that accepts `Uuid` arguments.",
    replaceWith = ReplaceWith("descriptorOf(Uuid.parse(service), Uuid.parse(characteristic), Uuid.parse(descriptor))"),
)
public fun descriptorOf(
    service: String,
    characteristic: String,
    descriptor: String,
): Descriptor = LazyDescriptor(
    serviceUuid = Uuid.parse(service),
    characteristicUuid = Uuid.parse(characteristic),
    descriptorUuid = Uuid.parse(descriptor),
)

public fun descriptorOf(service: Uuid, characteristic: Uuid, descriptor: Uuid): Descriptor =
    LazyDescriptor(service, characteristic, descriptor)

internal fun List<PlatformDiscoveredService>.obtain(
    characteristic: Characteristic,
    properties: Properties?,
): PlatformCharacteristic {
    if (characteristic is PlatformDiscoveredCharacteristic) return characteristic.characteristic

    val discoveredService = firstOrNull {
        it.serviceUuid == characteristic.serviceUuid
    } ?: throw NoSuchElementException("Service ${characteristic.serviceUuid} not found")

    val discoveredCharacteristic = discoveredService.characteristics.firstOrNull {
        it.characteristicUuid == characteristic.characteristicUuid &&
            (properties == null || (it.properties and properties).value != 0)
    } ?: throw NoSuchElementException("Characteristic ${characteristic.characteristicUuid}${properties.text}not found")

    return discoveredCharacteristic.characteristic
}

internal fun List<PlatformDiscoveredService>.obtain(
    descriptor: Descriptor,
): PlatformDescriptor {
    if (descriptor is PlatformDiscoveredDescriptor) return descriptor.descriptor

    val discoveredService = firstOrNull {
        it.serviceUuid == descriptor.serviceUuid
    } ?: throw NoSuchElementException("Service ${descriptor.serviceUuid} not found")

    val discoveredCharacteristic = discoveredService.characteristics.firstOrNull {
        it.characteristicUuid == descriptor.characteristicUuid
    } ?: throw NoSuchElementException("Characteristic ${descriptor.characteristicUuid} not found")

    val discoveredDescriptor = discoveredCharacteristic.descriptors.firstOrNull {
        it.descriptorUuid == descriptor.descriptorUuid
    } ?: throw NoSuchElementException("Descriptor ${descriptor.descriptorUuid} not found")

    return discoveredDescriptor.descriptor
}

private val Properties?.text: String
    get() = when (this?.value?.countOneBits()) {
        null, 0 -> " "
        1 -> " with $this property "
        else -> " with $this properties "
    }
