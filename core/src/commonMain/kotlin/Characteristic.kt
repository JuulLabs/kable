package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom

public fun characteristicOf(
    service: String,
    characteristic: String,
): Characteristic = LazyCharacteristic(
    serviceUuid = uuidFrom(service),
    characteristicUuid = uuidFrom(characteristic)
)

// todo: Drop `expect` when https://youtrack.jetbrains.com/issue/KTIJ-405 is fixed.
// On some source sets (when expect/actual is **not** used), IntelliJ highlights Uuid with an error similar to:
// > Property type is Uuid /* = UUID */, which is not a subtype type of overridden
// https://youtrack.jetbrains.com/issue/KTIJ-405
public expect interface Characteristic {
    public val serviceUuid: Uuid
    public val characteristicUuid: Uuid
}

public data class LazyCharacteristic internal constructor(
    override val serviceUuid: Uuid,
    override val characteristicUuid: Uuid,
) : Characteristic

public data class DiscoveredCharacteristic internal constructor(
    override val serviceUuid: Uuid,
    override val characteristicUuid: Uuid,
    public val descriptors: List<Descriptor>,
) : Characteristic
