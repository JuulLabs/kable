package com.juul.kable.external

import org.w3c.dom.events.Event

/** https://webbluetoothcg.github.io/web-bluetooth/#availability */
internal external class BluetoothAvailabilityChanged : Event {
    val value: Boolean // Bluetooth available.
}
