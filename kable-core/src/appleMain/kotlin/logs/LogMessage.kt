package com.juul.kable.logs

import com.juul.kable.logs.Logging.DataProcessor.Operation
import com.juul.kable.toByteArray
import com.juul.kable.toUuid
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBService
import platform.Foundation.NSData
import platform.Foundation.NSError

internal actual val LOG_INDENT: String? = "  "

internal fun LogMessage.detail(data: NSData?, operation: Operation) {
    detail(data?.toByteArray(), operation)
}

internal fun LogMessage.detail(error: NSError?) {
    if (error != null) detail("error", error.toString())
}

private fun LogMessage.detail(service: CBService? = null) {
    detail("service", service?.UUID?.UUIDString ?: "null UUID")
}

private fun LogMessage.detail(characteristic: CBCharacteristic) {
    val serviceUuid = characteristic.service
        ?.UUID
        ?.toUuid()
    if (serviceUuid == null) {
        detail("service", "null UUID")
        return
    }

    detail(serviceUuid, characteristic.UUID.toUuid())
}

private fun LogMessage.detail(descriptor: CBDescriptor) {
    val characteristic = descriptor.characteristic
    if (characteristic == null) {
        detail("characteristic", "null UUID")
        return
    }

    val serviceUuid = characteristic.service
        ?.UUID
        ?.toUuid()

    if (serviceUuid == null) {
        detail("service", "null UUID")
        return
    }

    detail(
        serviceUuid,
        characteristic.UUID.toUuid(),
        descriptor.UUID.toUuid(),
    )
}

internal fun LogMessage.detail(attribute: CBAttribute) {
    when (attribute) {
        is CBService -> detail(attribute)
        is CBCharacteristic -> detail(attribute)
        is CBDescriptor -> detail(attribute)
        else -> detail("unknown attribute", attribute.UUID.toUuid())
    }
}
