package com.juul.kable

import com.juul.kable.external.Bluetooth
import com.juul.kable.external.getBluetooth
import kotlin.js.undefined
import kotlinx.browser.window

/**
 * @return [Bluetooth] object or `null` if bluetooth is [unavailable](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth#browser_compatibility).
 */
internal fun bluetoothOrNull(): Bluetooth? {
    val navigator = window.navigator
    return getBluetooth(navigator).takeIf { it !== undefined }
}

/**
 * @throws IllegalStateException If bluetooth is [unavailable](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth#browser_compatibility).
 */
internal fun bluetoothOrThrow(): Bluetooth {
    val navigator = window.navigator
    val bluetooth = getBluetooth(navigator)
    if (bluetooth === undefined) {
        error("Bluetooth unavailable")
    }
    return bluetooth
}
