package com.juul.kable.logs

import com.benasher44.uuid.Uuid
import com.juul.kable.Characteristic
import com.juul.kable.Descriptor
import com.juul.kable.Service
import com.juul.kable.WriteType

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

    fun build(logging: Logging, platformIdentifier: String?): String = buildString {
        val prefix = logging.identifier ?: platformIdentifier
        if (!prefix.isNullOrEmpty()) {
            append(prefix)
            append(' ')
        }
        append(message)

        when (logging.format) {
            Logging.Format.Compact -> if (details.isNotEmpty()) append('(')
            Logging.Format.Multiline -> appendLine()
        }

        details.forEachIndexed { index, detail ->
            val (key, value) = detail

            if (value !is ByteArray || logging.level == Logging.Level.Data) {
                if (index > 0) {
                    when (logging.format) {
                        Logging.Format.Compact -> append(", ")
                        Logging.Format.Multiline -> appendLine()
                    }
                }

                if (logging.format == Logging.Format.Multiline && LOG_INDENT != null) append(LOG_INDENT)

                append(key)
                when (logging.format) {
                    Logging.Format.Compact -> append("=")
                    Logging.Format.Multiline -> append(": ")
                }
                if (value is ByteArray) append(logging.data.process(value)) else append(value)
            }
        }

        if (logging.format == Logging.Format.Compact && details.isNotEmpty()) append(')')
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
