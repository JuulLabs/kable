package com.juul.kable

import kotlin.uuid.Uuid

public actual typealias Identifier = Uuid

public actual fun String.toIdentifier(): Identifier = Uuid.parse(this)
