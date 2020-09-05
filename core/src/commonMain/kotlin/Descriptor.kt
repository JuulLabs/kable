package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom

public expect class Descriptor {
    public val uuid: Uuid
}

public operator fun List<Descriptor>?.get(
    uuid: String,
): Descriptor = getOrNull(uuid)
    ?: throw NoSuchElementException("Descriptor $uuid")

public fun List<Descriptor>?.getOrNull(
    uuid: String,
): Descriptor? {
    if (this == null) return null
    val searchUuid = uuidFrom(uuid)
    return firstOrNull { it.uuid == searchUuid }
}
