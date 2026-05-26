package com.juul.kable.external

import web.navigator.Navigator
import kotlin.js.js

/** Reference to [Bluetooth] instance or `null` if bluetooth is unavailable in this browser. */
internal fun getBluetooth(navigator: Navigator): Bluetooth? =
    js("navigator.bluetooth")
