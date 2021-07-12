@file:JvmName("AndroidLogger")

package com.juul.kable

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.util.Log
import com.juul.kable.gatt.GattStatus

internal actual val LOG_INDENT: String? = null

internal actual object SystemLogger {

    actual fun verbose(throwable: Throwable?, tag: String, message: String) {
        Log.v(tag, message, throwable)
    }

    actual fun debug(throwable: Throwable?, tag: String, message: String) {
        Log.d(tag, message, throwable)
    }

    actual fun info(throwable: Throwable?, tag: String, message: String) {
        Log.i(tag, message, throwable)
    }

    actual fun warn(throwable: Throwable?, tag: String, message: String) {
        Log.w(tag, message, throwable)
    }

    actual fun error(throwable: Throwable?, tag: String, message: String) {
        Log.e(tag, message, throwable)
    }

    actual fun assert(throwable: Throwable?, tag: String, message: String) {
        Log.wtf(tag, message, throwable)
    }
}

internal fun LogMessage.detail(status: GattStatus) {
    detail("status", status.toString())
}

internal fun LogMessage.detail(service: BluetoothGattService) {
    detail("service", service.uuid.toString())
}

internal fun LogMessage.detail(characteristic: BluetoothGattCharacteristic) {
    detail(characteristic.service)
    detail("characteristic", characteristic.uuid.toString())
}

internal fun LogMessage.detail(descriptor: BluetoothGattDescriptor) {
    detail(descriptor.characteristic)
    detail("descriptor", descriptor.uuid.toString())
}
