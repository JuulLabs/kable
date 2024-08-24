package com.juul.kable.external

import org.w3c.dom.Navigator

/** Reference to [Bluetooth] instance or [undefined] if bluetooth is unavailable. */
internal val Navigator.bluetooth: Bluetooth
    get() = asDynamic().bluetooth.unsafeCast<Bluetooth>()
