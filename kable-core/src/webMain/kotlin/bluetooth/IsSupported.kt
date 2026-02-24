package com.juul.kable.bluetooth

import com.juul.kable.InternalError
import com.juul.kable.bluetoothOrNull
import com.juul.kable.interop.await
import js.errors.TypeError
import kotlin.js.JsException
import kotlin.js.thrownValue

internal actual suspend fun isSupported(): Boolean {
    val bluetooth = bluetoothOrNull() ?: return false
    val promise = try {
        bluetooth.getAvailability()
    } catch (e: JsException) {
        // > TypeError: navigator.bluetooth.getAvailability is not a function
        if (e.thrownValue is TypeError) return false
        throw e
    }
    return try {
        promise.await()
    } catch (e: JsException) {
        throw InternalError("Failed to get bluetooth availability", e)
    }
}
