package com.juul.kable.logs

internal typealias LoggingBuilder = Logging.() -> Unit

public class Logging {

    public enum class Level {

        /** Logs warnings when unexpected failures occur. */
        Warnings,

        /** Same as [Warnings] plus all events. */
        Events,

        /** Same as [Events] plus hex representation of I/O data. */
        Data,
    }

    public enum class Format {

        /**
         * Outputs logging in compact format (on a single line per log), for example:
         *
         * ```
         * example message(detail1=value1, detail2=value2, ...)
         * ```
         */
        Compact,

        /**
         * Outputs logging in multiline format (spanning multiple lines for log details), for example:
         *
         * ```
         * example message
         *   detail1: value1
         *   detail2: value2
         *   ...
         * ```
         */
        Multiline,
    }

    public fun interface DataProcessor {
        public fun process(data: ByteArray): String
    }

    /**
     * Identifier to use in log messages. When `null`, defaults to the platform's peripheral identifier:
     *
     * - Android: Hardware (MAC) address (e.g. "00:11:22:AA:BB:CC")
     * - Apple: The UUID associated with the peer
     * - JavaScript: A `DOMString` that uniquely identifies a device
     */
    public var identifier: String? = null

    public var engine: LogEngine = SystemLogEngine
    public var level: Level = Level.Warnings
    public var format: Format = Format.Multiline
    public var data: DataProcessor = Hex
}
