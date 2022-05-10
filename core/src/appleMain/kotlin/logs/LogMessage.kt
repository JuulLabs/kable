package com.juul.kable.logs

import com.juul.kable.toByteArray
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBService
import platform.Foundation.NSData
import platform.Foundation.NSError

internal actual val LOG_INDENT: String? = "  "

internal fun LogMessage.detail(data: NSData?) {
    if (data != null) detail(data.toByteArray())
}

internal fun LogMessage.detail(error: NSError?) {
    if (error != null) detail("error", error.toString())
}

internal fun LogMessage.detail(service: CBService) {
    detail("service", service.UUID.UUIDString)
}

internal fun LogMessage.detail(characteristic: CBCharacteristic) {
    detail(characteristic.service!!)
    detail("characteristic", characteristic.UUID.UUIDString)
}

internal fun LogMessage.detail(descriptor: CBDescriptor) {
    detail(descriptor.characteristic!!)
    detail("descriptor", descriptor.UUID.UUIDString)
}
