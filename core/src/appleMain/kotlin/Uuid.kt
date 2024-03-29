package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSUUID

internal fun Uuid.toNSUUID(): NSUUID = NSUUID(toString())
internal fun Uuid.toCBUUID(): CBUUID = CBUUID.UUIDWithString(toString())
internal fun CBUUID.toUuid(): Uuid = when (UUIDString.length) {
    4 -> uuidFrom("0000$UUIDString-0000-1000-8000-00805F9B34FB")
    else -> uuidFrom(UUIDString)
}

internal fun NSUUID.toUuid(): Uuid = uuidFrom(UUIDString)
