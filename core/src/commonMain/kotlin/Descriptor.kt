package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom

public fun descriptorOf(
    serviceUuid: String,
    descriptorUuid: String,
    characteristicUuid: String,
): Descriptor = LazyDescriptor(
    serviceUuid = uuidFrom(serviceUuid),
    characteristicUuid = uuidFrom(characteristicUuid),
    descriptorUuid = uuidFrom(descriptorUuid)
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
