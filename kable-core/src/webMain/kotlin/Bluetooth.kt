package com.juul.kable

import com.juul.kable.external.Bluetooth
import kotlin.js.js

private val isBluetoothAvailable: Boolean
    get() = js("typeof window !== 'undefined' && typeof window.navigator !== 'undefined' && window.navigator.bluetooth !== 'undefined'")

/**
 * @return [Bluetooth] object or `null` if bluetooth is [unavailable](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth#browser_compatibility).
 */
internal fun bluetoothOrNull(): Bluetooth? =
    if (isBluetoothAvailable) js("window.navigator.bluetooth") else null

/**
 * @throws IllegalStateException If bluetooth is [unavailable](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth#browser_compatibility).
 */
internal fun bluetoothOrThrow(): Bluetooth =
    bluetoothOrNull() ?: error("Bluetooth unavailable")
