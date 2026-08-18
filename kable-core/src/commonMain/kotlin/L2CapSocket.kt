package com.juul.kable

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * A connection-oriented L2CAP channel (CoC) to a connected [Peripheral], opened with
 * [AndroidPeripheral.openL2CapChannel], [AndroidPeripheral.openInsecureL2CapChannel] or
 * [CoreBluetoothPeripheral.openL2CapChannel].
 *
 * A channel is a byte stream with no message boundaries, so callers must frame their own protocol.
 */
public interface L2CapSocket {

    /** Whether the channel is open. Becomes `false` once [closed][close], at end of stream, or on failure. */
    public val isConnected: StateFlow<Boolean>

    /**
     * Bytes received over the channel, in chunks of arbitrary size. Completes at end of stream and
     * throws [L2CapException] if the channel fails. Each chunk is delivered to a single collector.
     */
    public val incoming: Flow<ByteArray>

    /**
     * Writes all of [packet] to the channel.
     *
     * @throws IllegalArgumentException if [packet] is empty.
     * @throws L2CapException if the channel is closed or fails while writing.
     */
    @Throws(CancellationException::class, IOException::class)
    public suspend fun write(packet: ByteArray)

    /** Closes the channel, suspending until it is fully torn down. */
    @Throws(CancellationException::class, IOException::class)
    public suspend fun close()
}
