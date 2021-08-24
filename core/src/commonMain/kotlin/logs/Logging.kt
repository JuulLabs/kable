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

    public var engine: LogEngine = SystemLogEngine
    public var level: Level = Level.Warnings
    public var format: Format = Format.Multiline
    public var data: DataProcessor = Hex
}
