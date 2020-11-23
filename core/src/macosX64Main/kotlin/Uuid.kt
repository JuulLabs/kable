package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSUUID

internal fun Uuid.toNSUUID(): NSUUID = NSUUID(toString())
internal fun Uuid.toCBUUID(): CBUUID = CBUUID.UUIDWithString(toString())
internal fun CBUUID.toUuid(): Uuid = uuidFrom(UUIDString)
