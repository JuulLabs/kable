package com.juul.kable.logs

import com.benasher44.uuid.Uuid
import com.juul.kable.Characteristic
import com.juul.kable.Descriptor
import com.juul.kable.Service
import com.juul.kable.WriteType
import com.juul.kable.logs.Logging.Format.Compact
import com.juul.kable.logs.Logging.Format.Multiline

internal expect val LOG_INDENT: String?

internal class LogMessage(
    private val logging: Logging,
    platformIdentifier: String?,
    private val indent: String? = LOG_INDENT,
) {

    private val prefix = logging.identifier ?: platformIdentifier

    var message: String = ""
    private var serviceUuid: Uuid? = null
    private var characteristicUuid: Uuid? = null
    private var descriptorUuid: Uuid? = null
    private val details = mutableListOf<Pair<String, Any>>()
    var data: ByteArray? = null

    fun detail(service: Service) {
        serviceUuid = service.serviceUuid
        characteristicUuid = null
        descriptorUuid = null
    }

    fun detail(characteristic: Characteristic) {
        serviceUuid = characteristic.serviceUuid
        characteristicUuid = characteristic.characteristicUuid
        descriptorUuid = null
    }

    fun detail(descriptor: Descriptor) {
        serviceUuid = descriptor.serviceUuid
        characteristicUuid = descriptor.characteristicUuid
        descriptorUuid = descriptor.descriptorUuid
    }

    fun detail(key: String, value: String) {
        details += key to value
    }

    fun detail(key: String, value: Number) {
        detail(key, value.toString())
    }

    fun detail(key: String, value: Uuid) {
        detail(key, value.toString())
    }

    private var isFirst = true

    private fun StringBuilder.append(label: String, value: Any) {
        when (logging.format) {
            Compact -> if (isFirst) append('(') else append(", ")
            Multiline -> {
                appendLine()
                if (indent != null) append(indent)
            }
        }
        isFirst = false

        append(label)
        when (logging.format) {
            Compact -> append("=")
            Multiline -> append(": ")
        }
        append(value)
    }

    fun build(): String = buildString {
        if (!prefix.isNullOrEmpty()) {
            append(prefix)
            append(' ')
        }
        append(message)

        isFirst = true

        serviceUuid?.let { append("service", it) }
        characteristicUuid?.let { append("characteristic", it) }
        descriptorUuid?.let { append("descriptor", it) }

        details.forEach { (key, value) ->
            append(key, value)
        }

        if (logging.level == Logging.Level.Data) {
            data?.let {
                append("data", logging.data.process(it, serviceUuid, characteristicUuid, descriptorUuid))
            }
        }

        if (logging.format == Compact && !isFirst) append(')')
    }
}

internal fun LogMessage.detail(data: ByteArray?) {
    if (data != null) this@detail.data = data
}

internal fun LogMessage.detail(writeType: WriteType) {
    detail("writeType", writeType.name)
}
