package com.juul.kable

import com.juul.kable.external.BluetoothRemoteGATTCharacteristic
import com.juul.kable.external.BluetoothRemoteGATTService
import org.khronos.webgl.DataView

internal actual val LOG_INDENT: String? = "  "

internal actual object SystemLogger {

    actual fun verbose(throwable: Throwable?, tag: String, message: String) {
        debug(throwable, tag, message)
    }

    actual fun debug(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.asDynamic().debug("[%s] %s", tag, message)
        } else {
            console.asDynamic().debug("[%s] %s\n%o", tag, message, throwable)
        }
    }

    actual fun info(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.info("[%s] %s", tag, message)
        } else {
            console.info("[%s] %s\n%o", tag, message, throwable)
        }
    }

    actual fun warn(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.warn("[%s] %s", tag, message)
        } else {
            console.warn("[%s] %s\n%o", tag, message, throwable)
        }
    }

    actual fun error(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.error("[%s] %s", tag, message)
        } else {
            console.error("[%s] %s\n%o", tag, message, throwable)
        }
    }

    actual fun assert(throwable: Throwable?, tag: String, message: String) {
        if (throwable == null) {
            console.asDynamic().assert(false, "[%s] %s", tag, message)
        } else {
            console.asDynamic().assert(false, "[%s] %s\n%o", tag, message, throwable)
        }
    }
}

internal fun LogMessage.detail(data: DataView?) {
    if (data != null) detail(data.buffer.toByteArray())
}

internal fun LogMessage.detail(service: BluetoothRemoteGATTService) {
    detail("service", service.uuid)
}

internal fun LogMessage.detail(characteristic: BluetoothRemoteGATTCharacteristic) {
    detail(characteristic.service)
    detail("characteristic", characteristic.uuid)
}
