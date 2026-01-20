package com.juul.kable.external

import web.navigator.Navigator
import kotlin.js.js

/** Reference to [Bluetooth] instance or [undefined] if bluetooth is unavailable. */
internal fun getBluetooth(navigator: Navigator): Bluetooth =
    js("navigator.bluetooth")
