package com.juul.kable

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
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
 * imposes no message boundaries, so callers are responsible for framing their own protocol. In
 * particular, the chunks returned by [read] are arbitrary runs of bytes — their sizes carry no meaning.
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
     * Reads the next chunk of bytes from the channel. Suspends until at least one byte is available,
     * end-of-stream is reached, or an error occurs. The returned array is freshly allocated and owned by
     * the caller. Cancelling a read does not lose data: a chunk already being read is returned by the
     * next call.
     *
     * @return the next chunk (at least one byte), or `null` once end-of-stream has been reached.
     * @throws L2CapException if the channel fails while reading.
     */
    @Throws(CancellationException::class, IOException::class)
    public suspend fun read(): ByteArray?

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

/**
 * The channel's incoming bytes as a cold [Flow] of chunks, completing at end-of-stream. Collection
 * calls [read], so collect from at most one coroutine and do not call [read] while collecting.
 */
public fun L2CapSocket.incoming(): Flow<ByteArray> = flow {
    while (true) emit(read() ?: break)
}
