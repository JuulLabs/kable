package com.juul.kable.external

import kotlin.js.js
import org.w3c.dom.Navigator

/** Reference to [Bluetooth] instance or [undefined] if bluetooth is unavailable. */
internal fun getBluetooth(navigator: Navigator): Bluetooth =
    js("navigator.bluetooth")
