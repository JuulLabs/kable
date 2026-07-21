package com.juul.kable.server

import com.juul.kable.Identifier
import platform.CoreBluetooth.CBCentral
import kotlin.uuid.Uuid

internal class AppleCentral(
    internal val cbCentral: CBCentral,
) : Central {

    override val identifier: Identifier = Uuid.parse(cbCentral.identifier.UUIDString)

    override val maximumNotificationLength: Int
        get() = cbCentral.maximumUpdateValueLength.toInt()

    override fun equals(other: Any?): Boolean =
        other is AppleCentral && other.identifier == identifier

    override fun hashCode(): Int = identifier.hashCode()

    override fun toString(): String = "Central(identifier=$identifier)"
}
