package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom

public fun descriptorOf(
    service: String,
    characteristic: String,
    descriptor: String,
): Descriptor = LazyDescriptor(
    serviceUuid = uuidFrom(service),
    characteristicUuid = uuidFrom(characteristic),
    descriptorUuid = uuidFrom(descriptor)
)

public interface Descriptor {
    public val serviceUuid: Uuid
    public val characteristicUuid: Uuid
    public val descriptorUuid: Uuid
}

public data class LazyDescriptor(
    public override val serviceUuid: Uuid,
    public override val characteristicUuid: Uuid,
    public override val descriptorUuid: Uuid,
) : Descriptor

internal fun <T : Descriptor> List<T>.first(
    descriptorUuid: Uuid
): T = firstOrNull(descriptorUuid)
    ?: throw NoSuchElementException("Descriptor $descriptorUuid not found")

internal fun <T : Descriptor> List<T>.firstOrNull(
    descriptorUuid: Uuid
): T? = firstOrNull { it.descriptorUuid == descriptorUuid }
