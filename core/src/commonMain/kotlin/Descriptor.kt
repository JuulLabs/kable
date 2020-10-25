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

// todo: Drop `expect` when https://youtrack.jetbrains.com/issue/KTIJ-405 is fixed.
// On some source sets (when expect/actual is **not** used), IntelliJ highlights Uuid with an error similar to:
// > Property type is Uuid /* = UUID */, which is not a subtype type of overridden
// https://youtrack.jetbrains.com/issue/KTIJ-405
public expect interface Descriptor {
    public val serviceUuid: Uuid
    public val characteristicUuid: Uuid
    public val descriptorUuid: Uuid
}

public data class LazyDescriptor(
    public override val serviceUuid: Uuid,
    public override val characteristicUuid: Uuid,
    public override val descriptorUuid: Uuid,
) : Descriptor
