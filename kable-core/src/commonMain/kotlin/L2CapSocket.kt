package com.juul.kable

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * An L2CAP connection-oriented channel, opened with [Peripheral.openL2CapChannel].
 *
 * A channel is a byte stream: data may arrive in chunks of any size, regardless of how the peer
 * wrote it, so protocols must do their own framing.
 */
public interface L2CapSocket {

    /**
     * `true` until the channel is [closed][close] (by either side) or the connection to the
     * peripheral ends.
     */
    public val isConnected: StateFlow<Boolean>

    /**
     * Data received over the channel. Every collector receives every chunk.
     *
     * Chunks received before the first collector subscribes are delivered to it; after that, chunks
     * received while there are no collectors are dropped. Chunks are buffered without bound for slow
     * collectors. Never completes, use [isConnected] to detect when the channel closes.
     */
    public val incoming: SharedFlow<ByteArray>

    /**
     * Writes all of [packet], suspending until it has been written.
     *
     * @throws IllegalArgumentException if [packet] is empty.
     * @throws IOException if the channel is closed or fails.
     */
    @Throws(CancellationException::class, IOException::class)
    public suspend fun write(packet: ByteArray)

    /** Closes the channel, suspending until it is closed. */
    @Throws(CancellationException::class, IOException::class)
    public suspend fun close()
}
