package com.juul.kable

import com.juul.kable.external.Bluetooth
import kotlin.js.js

private fun isBluetoothSupported(): Boolean =
    js("typeof window !== 'undefined' && typeof window.navigator !== 'undefined' && window.navigator.bluetooth")

private fun jsBluetooth(): Bluetooth = js("window.navigator.bluetooth")

/**
 * @return [Bluetooth] object or `null` if bluetooth is [unsupported](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth#browser_compatibility).
 */
internal fun bluetoothOrNull(): Bluetooth? =
    if (isBluetoothSupported()) jsBluetooth() else null

/**
 * @throws IllegalStateException If bluetooth is [unsupported](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth#browser_compatibility).
 */
internal fun bluetoothOrThrow(): Bluetooth =
    bluetoothOrNull() ?: error("Bluetooth unavailable")
