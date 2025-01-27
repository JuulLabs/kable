package com.juul.kable

import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSUUID
import kotlin.uuid.Uuid

internal fun Uuid.toNSUUID(): NSUUID = NSUUID(toString())
internal fun Uuid.toCBUUID(): CBUUID = CBUUID.UUIDWithString(toString())
internal fun CBUUID.toUuid(): Uuid = UUIDString
    .lowercase()
    .let {
        when (it.length) {
            4 -> Uuid.parse("0000$it-0000-1000-8000-00805f9b34fb")
            8 -> Uuid.parse("$it-0000-1000-8000-00805f9b34fb")
            else -> Uuid.parse(it)
        }
    }

internal fun NSUUID.toUuid(): Uuid = Uuid.parse(UUIDString)
