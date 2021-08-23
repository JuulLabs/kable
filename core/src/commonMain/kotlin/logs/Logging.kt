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

    public class Data {

        /** Separator between each byte in the hex representation of data (when [Data][Level.Data] log level is used). */
        public var separator: String = " "

        /** Configures if hex representation of data (when [Data][Level.Data] log level is used) should be lower-case. */
        public var lowerCase: Boolean = false
    }

    internal var data: Data = Data()
    public fun data(init: Data.() -> Unit) {
        val data = Data()
        data.init()
        this.data = data
    }

    public var engine: LogEngine = SystemLogEngine
    public var level: Level = Level.Warnings
    public var format: Format = Format.Multiline
}
