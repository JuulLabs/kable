package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom

public expect class Service {
    public val uuid: Uuid
    public val characteristics: List<Characteristic>
}

public operator fun List<Service>?.get(
    uuid: String
): Service = getOrNull(uuid)
    ?: throw NoSuchElementException("Service $uuid")

public fun List<Service>?.getOrNull(
    uuid: String
): Service? {
    if (this == null) return null
    val searchUuid = uuidFrom(uuid)
    return firstOrNull { it.uuid == searchUuid }
}
