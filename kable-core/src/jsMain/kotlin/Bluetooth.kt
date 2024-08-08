package com.juul.kable

import com.juul.kable.external.Bluetooth
import com.juul.kable.external.bluetooth
import js.errors.ReferenceError
import kotlinx.browser.window

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
