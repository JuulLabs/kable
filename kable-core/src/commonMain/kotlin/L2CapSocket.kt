package com.juul.kable

import kotlinx.coroutines.flow.StateFlow
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * A connection-oriented L2CAP channel (CoC) to a connected [Peripheral].
 *
 * Obtain a socket from a platform-specific peripheral — [AndroidPeripheral.openL2CapChannel] /
 * [AndroidPeripheral.openInsecureL2CapChannel] on Android, or [CoreBluetoothPeripheral.openL2CapChannel]
 * on Apple platforms. The returned socket is already open and ready for I/O.
 *
 * Unlike GATT's message-based characteristics, an L2CAP channel is a bidirectional byte stream: it
 * imposes no message boundaries, so callers are responsible for framing their own protocol.
 *
 * [read] is expected to be called from a single coroutine at a time; [write] may be called concurrently
 * with [read].
 */
public interface L2CapSocket {

    /**
     * Whether the socket is currently open for reading and writing. Becomes `false` once the socket is
     * [closed][close], or when the channel reaches end-of-stream or fails.
     */
    public val isConnected: StateFlow<Boolean>

    /**
     * Whether the read side has reached end-of-stream. Once `true`, [read] returns `-1` and no further
     * data will arrive.
     */
    public val hasReachedEof: StateFlow<Boolean>

    /**
     * Reads bytes from the channel into [buffer]. Suspends until at least one byte is available,
     * end-of-stream is reached, or an error occurs.
     *
     * @return the number of bytes read into [buffer] (at least `1`), `0` if [buffer] is empty, or `-1`
     * once end-of-stream has been reached.
     * @throws L2CapException if the channel fails while reading.
     */
    @Throws(CancellationException::class, IOException::class)
    public suspend fun read(buffer: ByteArray): Int

    /**
     * Writes the entirety of [packet] to the channel, suspending until it has all been handed off to the
     * operating system. A no-op if [packet] is empty.
     *
     * @throws L2CapException if the channel is closed or fails while writing.
     */
    @Throws(CancellationException::class, IOException::class)
    public suspend fun write(packet: ByteArray)

    /** Closes the channel and releases its resources, suspending until teardown is complete. */
    @Throws(CancellationException::class, IOException::class)
    public suspend fun close()
}
