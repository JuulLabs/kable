package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom

public expect class Service {
    public val uuid: Uuid
    public val characteristics: List<Characteristic>
}

public operator fun List<Characteristic>.get(
    uuid: String,
): Characteristic {
    val searchUuid = uuidFrom(uuid)
    return firstOrNull { it.uuid == searchUuid }
        ?: throw NoSuchElementException("Characteristic $uuid not found.")
}
