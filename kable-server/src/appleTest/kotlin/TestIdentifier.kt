package com.juul.kable.server

import com.juul.kable.Identifier
import kotlin.uuid.Uuid

internal actual fun testIdentifier(seed: Int): Identifier {
    require(seed in 0..255)
    return Uuid.fromLongs(0L, seed.toLong())
}
