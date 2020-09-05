package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom

public enum class WriteType {
    WithResponse,
    WithoutResponse,
}

public expect class Characteristic {
    public val uuid: Uuid
    public val descriptors: List<Descriptor>
}

public operator fun List<Characteristic>?.get(
    uuid: String,
): Characteristic = getOrNull(uuid)
    ?: throw NoSuchElementException("Characteristic $uuid")

public fun List<Characteristic>?.getOrNull(
    uuid: String,
): Characteristic? {
    if (this == null) return null
    val searchUuid = uuidFrom(uuid)
    return firstOrNull { it.uuid == searchUuid }
}
