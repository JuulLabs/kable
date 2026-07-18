package com.juul.kable.server

import com.juul.kable.Identifier

/** Creates a (platform specific) [Identifier] unique to [seed] (for `0 <= seed <= 255`). */
internal expect fun testIdentifier(seed: Int): Identifier

internal class FakeCentral(seed: Int) : Central {

    override val identifier: Identifier = testIdentifier(seed)

    override val maximumNotificationLength: Int = 20

    override fun equals(other: Any?): Boolean =
        other is FakeCentral && other.identifier == identifier

    override fun hashCode(): Int = identifier.hashCode()

    override fun toString(): String = "FakeCentral(identifier=$identifier)"
}
