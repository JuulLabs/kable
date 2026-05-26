package com.juul.kable

import com.juul.kable.external.Bluetooth
import com.juul.kable.external.getBluetooth
import web.navigator.Navigator
import kotlin.js.js

private val navigator: Navigator =
    js("window.navigator")

/**
 * @return [Bluetooth] object or `null` if bluetooth is [unavailable](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth#browser_compatibility).
 */
internal fun bluetoothOrNull(): Bluetooth? =
    getBluetooth(navigator)

/**
 * @throws IllegalStateException If bluetooth is [unavailable](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth#browser_compatibility).
 */
internal fun bluetoothOrThrow(): Bluetooth =
    getBluetooth(navigator) ?: error("Bluetooth unavailable")
