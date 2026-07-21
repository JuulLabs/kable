package com.juul.kable.server.logs

import com.juul.kable.logs.Logging
import com.juul.kable.logs.Logging.Level.Data
import com.juul.kable.logs.Logging.Level.Warnings

/**
 * Minimal logger for `kable-server` (`kable-core`'s `Logger` is `internal` to `kable-core`),
 * honoring the [Logging] configuration provided via the `logging` DSL.
 */
internal class Logger(
    private val logging: Logging,
    private val tag: String = "Kable/GattServer",
) {

    private val prefix: String
        get() = logging.identifier?.let { "[$it] " }.orEmpty()

    /** Logged only when [Logging.level] is [events][Logging.Level.Events] (or higher). */
    inline fun debug(message: () -> String) {
        if (logging.level != Warnings) logging.engine.debug(null, tag, prefix + message())
    }

    inline fun warn(cause: Throwable? = null, message: () -> String) {
        logging.engine.warn(cause, tag, prefix + message())
    }

    /**
     * String representation of I/O [data] for inclusion in log messages, or empty when
     * [Logging.level] is not [Data].
     */
    @Suppress("OPT_IN_USAGE") // `Logging.data` is `@ObsoleteKableApi`.
    fun data(data: ByteArray): String =
        if (logging.level == Data) " data=${logging.data.process(data, null, null, null, null)}" else ""
}
