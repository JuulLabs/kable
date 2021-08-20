package com.juul.kable.logs

import com.benasher44.uuid.Uuid
import com.juul.kable.Characteristic
import com.juul.kable.Descriptor
import com.juul.kable.Service
import com.juul.kable.WriteType
import com.juul.kable.toHexString

internal expect val LOG_INDENT: String?

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
            Logging.Format.Compact -> if (details.isNotEmpty()) append('(')
            Logging.Format.Multiline -> appendLine()
        }

        details.forEachIndexed { index, detail ->
            val (key, value) = detail

            if (logging.format == Logging.Format.Multiline && LOG_INDENT != null) append(LOG_INDENT)
            append(key)
            when (logging.format) {
                Logging.Format.Compact -> append("=")
                Logging.Format.Multiline -> append(": ")
            }

            if (value is ByteArray) {
                if (logging.level == Logging.Level.Data) {
                    val separator = logging.data.separator
                    val lowerCase = logging.data.lowerCase
                    append(value.toHexString(separator = separator, lowerCase = lowerCase))
                }
            } else {
                append(value)
            }

            when (logging.format) {
                Logging.Format.Compact -> if (index < details.lastIndex) append(", ") else append(')')
                Logging.Format.Multiline -> if (index < details.lastIndex) appendLine()
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
