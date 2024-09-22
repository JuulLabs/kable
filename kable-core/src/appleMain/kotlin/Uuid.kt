@file:OptIn(ExperimentalUuidApi::class)

package com.juul.kable

import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSUUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal fun Uuid.toNSUUID(): NSUUID = NSUUID(toString())
internal fun Uuid.toCBUUID(): CBUUID = CBUUID.UUIDWithString(toString())
internal fun CBUUID.toUuid(): Uuid = when (UUIDString.length) {
    4 -> Uuid.parse("0000$UUIDString-0000-1000-8000-00805F9B34FB")
    else -> Uuid.parse(UUIDString)
}

internal fun NSUUID.toUuid(): Uuid = Uuid.parse(UUIDString)
