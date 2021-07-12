package com.juul.kable

import com.juul.kable.Logging.Format.Multiline
import com.juul.kable.Logging.Level.Warnings

public expect class ServicesDiscoveredPeripheral {

    public suspend fun read(
        characteristic: Characteristic,
    ): ByteArray

    public suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType = WriteType.WithoutResponse,
    ): Unit

    public suspend fun read(
        descriptor: Descriptor,
    ): ByteArray

    public suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ): Unit
}

internal typealias ServicesDiscoveredAction = suspend ServicesDiscoveredPeripheral.() -> Unit

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

    public var level: Level = Warnings
    public var format: Format = Multiline
}

internal typealias LoggingBuilder = Logging.() -> Unit

public expect class PeripheralBuilder internal constructor() {
    public fun logging(init: LoggingBuilder)
    public fun onServicesDiscovered(action: ServicesDiscoveredAction)
}
