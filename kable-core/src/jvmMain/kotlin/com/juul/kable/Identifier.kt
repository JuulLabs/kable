package com.juul.kable

import com.juul.kable.btleplug.ffi.PeripheralId

/** Depending on the OS, this is either a UUID (Apple), MAC address (Windows), or path (Linux). */
public actual class Identifier(internal val ffi: PeripheralId) {
    override fun equals(other: Any?): Boolean = other is Identifier && other.ffi == ffi
    override fun hashCode(): Int = ffi.hashCode()
    override fun toString(): String = ffi.toString()
}

public actual fun String.toIdentifier(): Identifier = Identifier(PeripheralId(this))
