package com.juul.kable

import com.benasher44.uuid.Uuid

public interface Service {
    public val serviceUuid: Uuid
}

public data class DiscoveredService internal constructor(
    override val serviceUuid: Uuid,
    public val characteristics: List<DiscoveredCharacteristic>,
) : Service

/** @throws IOException if service is not found. */
internal fun <T : Service> List<T>.first(
    serviceUuid: Uuid
): T = firstOrNull { it.serviceUuid == serviceUuid }
    ?: throw IOException("Service $serviceUuid not found")
