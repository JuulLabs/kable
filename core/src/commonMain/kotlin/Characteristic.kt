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

public operator fun List<Descriptor>.get(
    uuid: String,
): Descriptor {
    val searchUuid = uuidFrom(uuid)
    return firstOrNull { it.uuid == searchUuid }
        ?: throw NoSuchElementException("Descriptor $uuid not found.")
}
