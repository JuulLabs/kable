package com.juul.kable.server

import com.juul.kable.Identifier

internal actual fun testIdentifier(seed: Int): Identifier {
    require(seed in 0..255)
    val octet = seed.toString(16).padStart(2, '0').uppercase()
    return "00:11:22:33:44:$octet"
}
