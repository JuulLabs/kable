package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import java.util.UUID

internal fun Uuid.toUUID(): UUID = UUID.fromString(toString())
internal fun UUID.toUuid(): Uuid = uuidFrom(toString())
