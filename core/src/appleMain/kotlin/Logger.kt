package com.juul.kable

import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBService
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSLog

internal actual val LOG_INDENT: String? = "  "

internal actual object SystemLogger {

    actual fun verbose(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            NSLog("V/%s: %s", tag, message)
        } else {
            NSLog("V/%s: %s\n%s", tag, message, throwable.stackTraceToString())
        }
    }

    actual fun debug(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            NSLog("D/%s: %s", tag, message)
        } else {
            NSLog("D/%s: %s\n%s", tag, message, throwable.stackTraceToString())
        }
    }

    actual fun info(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            NSLog("I/%s: %s", tag, message)
        } else {
            NSLog("I/%s: %s\n%s", tag, message, throwable.stackTraceToString())
        }
    }

    actual fun warn(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            NSLog("W/%s: %s", tag, message)
        } else {
            NSLog("W/%s: %s\n%s", tag, message, throwable.stackTraceToString())
        }
    }

    actual fun error(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            NSLog("E/%s: %s", tag, message)
        } else {
            NSLog("E/%s: %s\n%s", tag, message, throwable.stackTraceToString())
        }
    }

    actual fun assert(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            NSLog("A/%s: %s", tag, message)
        } else {
            NSLog("A/%s: %s\n%s", tag, message, throwable.stackTraceToString())
        }
    }
}

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
    detail(characteristic.service)
    detail("characteristic", characteristic.UUID.UUIDString)
}

internal fun LogMessage.detail(descriptor: CBDescriptor) {
    detail(descriptor.characteristic)
    detail("descriptor", descriptor.UUID.UUIDString)
}
