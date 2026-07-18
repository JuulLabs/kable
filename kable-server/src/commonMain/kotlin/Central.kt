package com.juul.kable.server

import com.juul.kable.ExperimentalKableApi
import com.juul.kable.Identifier

/**
 * Represents a remote device (acting in the Bluetooth Low Energy central role) interacting with the
 * local [GattServer].
 *
 * Instances are equal when they represent the same remote device (i.e. have the same [identifier]).
 */
@ExperimentalKableApi
public interface Central {

    /**
     * Platform specific identifier of the remote central:
     *
     * - Android: Hardware (MAC) address (e.g. "00:11:22:AA:BB:CC")
     * - Apple: The UUID associated with the peer
     */
    public val identifier: Identifier

    /**
     * Maximum number of bytes that can be carried in a single notification (or indication) to this
     * central (i.e. current ATT MTU size, minus the size of the ATT headers — 3 bytes).
     */
    public val maximumNotificationLength: Int
}
