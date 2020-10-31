package com.juul.kable

import com.benasher44.uuid.Uuid

public interface Service {
    public val serviceUuid: Uuid
}

public data class DiscoveredService internal constructor(
    override val serviceUuid: Uuid,
    public val characteristics: List<DiscoveredCharacteristic>,
) : Service
