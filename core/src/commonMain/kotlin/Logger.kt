package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.Logging.Format.Compact
import com.juul.kable.Logging.Format.Multiline
import com.juul.kable.Logging.Level.Data
import com.juul.kable.Logging.Level.Events

internal expect val LOG_INDENT: String?

internal expect object SystemLogger {
    fun verbose(throwable: Throwable?, tag: String, message: String)
    fun debug(throwable: Throwable?, tag: String, message: String)
    fun info(throwable: Throwable?, tag: String, message: String)
    fun warn(throwable: Throwable?, tag: String, message: String)
    fun error(throwable: Throwable?, tag: String, message: String)
    fun assert(throwable: Throwable?, tag: String, message: String)
}

internal class LogMessage {

    var message: String = ""

    private val details = mutableListOf<Pair<String, Any>>()

    fun detail(key: String, value: String) {
        details += key to value
    }

    fun detail(key: String, value: ByteArray) {
        details += key to value
    }

    fun detail(key: String, value: Number) {
        detail(key, value.toString())
    }

    fun detail(key: String, value: Uuid) {
        detail(key, value.toString())
    }

    fun build(logging: Logging, prefix: String?): String = buildString {
        if (prefix != null) append(prefix)
        append(message)

        when (logging.format) {
            Compact -> if (details.isNotEmpty()) append('(')
            Multiline -> appendLine()
        }

        details.forEachIndexed { index, detail ->
            val (key, value) = detail

            if (logging.format == Multiline && LOG_INDENT != null) append(LOG_INDENT)
            append(key)
            when (logging.format) {
                Compact -> append("=")
                Multiline -> append(": ")
            }

            if (value is ByteArray) {
                if (logging.level == Data) {
                    val separator = logging.data.separator
                    val lowerCase = logging.data.lowerCase
                    append(value.toHexString(separator = separator, lowerCase = lowerCase))
                }
            } else {
                append(value)
            }

            when (logging.format) {
                Compact -> if (index < details.lastIndex) append(", ") else append(')')
                Multiline -> if (index < details.lastIndex) appendLine()
            }
        }
    }
}

internal fun LogMessage.detail(data: ByteArray?) {
    if (data != null) detail("data", data)
}

internal fun LogMessage.detail(service: Service) {
    detail("service", service.serviceUuid)
}

internal fun LogMessage.detail(characteristic: Characteristic) {
    detail("service", characteristic.serviceUuid)
    detail("characteristic", characteristic.characteristicUuid)
}

internal fun LogMessage.detail(descriptor: Descriptor) {
    detail("service", descriptor.serviceUuid)
    detail("characteristic", descriptor.characteristicUuid)
    detail("descriptor", descriptor.descriptorUuid)
}

internal fun LogMessage.detail(writeType: WriteType) {
    detail("writeType", writeType.name)
}

internal class Logger(
    private val logging: Logging,
    private val tag: String = "Kable",
    private val prefix: String? = null,
) {

    inline fun verbose(throwable: Throwable? = null, crossinline init: LogMessage.() -> Unit) {
        if (logging.level == Events || logging.level == Data) {
            val message = LogMessage()
            message.init()
            SystemLogger.verbose(throwable, tag, message.build(logging, prefix))
        }
    }

    inline fun debug(throwable: Throwable? = null, crossinline init: LogMessage.() -> Unit) {
        if (logging.level == Events || logging.level == Data) {
            val message = LogMessage()
            message.init()
            SystemLogger.debug(throwable, tag, message.build(logging, prefix))
        }
    }

    inline fun info(throwable: Throwable? = null, crossinline init: LogMessage.() -> Unit) {
        if (logging.level == Events || logging.level == Data) {
            val message = LogMessage()
            message.init()
            SystemLogger.info(throwable, tag, message.build(logging, prefix))
        }
    }

    inline fun warn(throwable: Throwable? = null, crossinline init: LogMessage.() -> Unit) {
        val message = LogMessage()
        message.init()
        SystemLogger.warn(throwable, tag, message.build(logging, prefix))
    }

    inline fun error(throwable: Throwable? = null, crossinline init: LogMessage.() -> Unit) {
        val message = LogMessage()
        message.init()
        SystemLogger.error(throwable, tag, message.build(logging, prefix))
    }

    inline fun assert(throwable: Throwable? = null, crossinline init: LogMessage.() -> Unit) {
        val message = LogMessage()
        message.init()
        SystemLogger.assert(throwable, tag, message.build(logging, prefix))
    }
}
