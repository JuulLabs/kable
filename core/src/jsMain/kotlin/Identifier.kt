package com.juul.kable

public actual typealias Identifier = String

public actual fun String.toIdentifier(): Identifier = this
