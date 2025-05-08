package com.juul.kable

import com.juul.kable.btleplug.ffi.PeripheralId

/** Depending on the OS, this is either a UUID (Apple) or a mac-address (Linux, Windows). */
public actual typealias Identifier = PeripheralId

public actual fun String.toIdentifier(): Identifier = PeripheralId(this)
