package com.juul.kable

import kotlinx.coroutines.flow.StateFlow

public interface L2CapSocket {
    /**
     * Contains whether the socket has been setup and it can be used to receive and send packets.
     */
    public val isReady: StateFlow<Boolean>

    /**
     * Reads up to [maxBytesToRead] from the socket and returns the a [ByteArray] of size equal to
     * the amount of bytes read. The size of the returned [ByteArray] is <= [maxBytesToRead]
     */
    public suspend fun receive(maxBytesToRead: Int = Int.MAX_VALUE): ByteArray

    /**
     * Sends a [ByteArray] through the socket and returns how many bytes were sent. The returned
     * value is <= [packet] size
     */
    public suspend fun send(packet: ByteArray): Long
}