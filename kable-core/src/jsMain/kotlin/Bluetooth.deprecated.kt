package com.juul.kable

import com.juul.kable.external.Bluetooth

@Deprecated(
    message = "Replaced with `bluetoothOrThrow` function.",
    replaceWith = ReplaceWith("bluetoothOrThrow()"),
)
@Suppress("UnsafeCastFromDynamic")
internal val bluetoothDeprecated: Bluetooth
    get() = checkNotNull(safeWebBluetooth) { "Bluetooth unavailable" }

// In a node build environment (e.g. unit test) there is no window, guard for that to avoid build errors.
private val safeWebBluetooth: dynamic =
    js("typeof(window) !== 'undefined' && window.navigator.bluetooth")
