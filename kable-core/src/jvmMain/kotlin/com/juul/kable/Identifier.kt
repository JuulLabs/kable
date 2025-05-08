package com.juul.kable

/** Depending on the OS, this is either a UUID (Apple) or a mac-address (Linux, Windows). */
public actual typealias Identifier = String

public actual fun String.toIdentifier(): Identifier = this
