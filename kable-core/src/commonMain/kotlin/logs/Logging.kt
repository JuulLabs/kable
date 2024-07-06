package com.juul.kable.logs

import com.benasher44.uuid.Uuid
import com.juul.kable.ObsoleteKableApi

public typealias LoggingBuilder = Logging.() -> Unit

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

    @ObsoleteKableApi // Planned to be replaced w/ I/O interceptors: https://github.com/JuulLabs/kable/issues/539
    public fun interface DataProcessor {

        public enum class Operation { Read, Write, Change }

        public fun process(
            data: ByteArray,
            operation: Operation?,
            serviceUuid: Uuid?,
            characteristicUuid: Uuid?,
            descriptorUuid: Uuid?,
        ): String
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

    @ObsoleteKableApi // Planned to be replaced w/ I/O interceptors: https://github.com/JuulLabs/kable/issues/539
    public var data: DataProcessor = Hex
}
