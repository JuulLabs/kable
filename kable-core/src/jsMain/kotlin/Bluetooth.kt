package com.juul.kable

import com.juul.kable.Bluetooth.State
import com.juul.kable.external.Bluetooth
import com.juul.kable.external.bluetooth
import js.errors.JsError
import js.errors.ReferenceError
import js.errors.TypeError
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * @return [Bluetooth] object or `null` if bluetooth is [unavailable](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth#browser_compatibility).
 */
internal fun bluetoothOrNull(): Bluetooth? {
    val navigator = try {
        window.navigator
    } catch (e: ReferenceError) {
        // ReferenceError: window is not defined
        return null
    }
    return navigator.bluetooth.takeIf { it !== undefined }
}

/**
 * @throws IllegalStateException If bluetooth is [unavailable](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth#browser_compatibility).
 */
internal fun bluetoothOrThrow(): Bluetooth {
    val navigator = try {
        window.navigator
    } catch (e: ReferenceError) {
        // ReferenceError: window is not defined
        throw IllegalStateException("Bluetooth unavailable", e)
    }
    val bluetooth = navigator.bluetooth
    if (bluetooth === undefined) {
        error("Bluetooth unavailable")
    }
    return bluetooth
}

internal actual suspend fun isBluetoothSupported(): Boolean {
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

internal actual val bluetoothState: Flow<State> = flow {
    if (!isBluetoothSupported()) error("Bluetooth is not supported")
    emit(State.On)
    // FIXME: awaitCancellation() ?
}
