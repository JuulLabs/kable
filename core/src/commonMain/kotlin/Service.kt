package com.juul.kable

import com.benasher44.uuid.Uuid

// todo: Drop `expect` when https://youtrack.jetbrains.com/issue/KTIJ-405 is fixed.
// On some source sets (when expect/actual is **not** used), IntelliJ highlights Uuid with an error similar to:
// > Property type is Uuid /* = UUID */, which is not a subtype type of overridden
// https://youtrack.jetbrains.com/issue/KTIJ-405
public expect interface Service {
    public val serviceUuid: Uuid
}

public data class DiscoveredService internal constructor(
    override val serviceUuid: Uuid,
    public val characteristics: List<DiscoveredCharacteristic>,
) : Service
