package com.juul.kable.external

import org.w3c.dom.events.Event

/**
 * https://webbluetoothcg.github.io/web-bluetooth/#bluetoothadvertisingevent
 */
internal abstract external class BluetoothAdvertisingEvent : Event {
    val rssi: Int
    val device: BluetoothDevice
}
