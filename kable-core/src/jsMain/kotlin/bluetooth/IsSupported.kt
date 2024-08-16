package com.juul.kable.bluetooth

import com.juul.kable.InternalException
import com.juul.kable.bluetoothOrNull
import js.errors.JsError
import js.errors.TypeError
import kotlinx.coroutines.await

internal actual suspend fun isSupported(): Boolean {
    val bluetooth = bluetoothOrNull() ?: return false
    val promise = try {
        bluetooth.getAvailability()
    } catch (e: TypeError) {
        // > TypeError: navigator.bluetooth.getAvailability is not a function
        return false
    }
    return try {
        promise.await()
    } catch (e: JsError) {
        throw InternalException("Failed to get bluetooth availability", e)
    }
}
