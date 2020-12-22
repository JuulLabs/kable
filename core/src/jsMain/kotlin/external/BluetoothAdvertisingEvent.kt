package com.juul.kable.external

import org.khronos.webgl.DataView
import org.w3c.dom.events.Event

/**
 * https://webbluetoothcg.github.io/web-bluetooth/#bluetoothadvertisingevent
 */
internal abstract external class BluetoothAdvertisingEvent : Event {
    val device: BluetoothDevice
    val uuids: Array<String>
    val name: String?
    val rssi: Int
    val manufacturerData: Any?
    val serviceData: Any?
}
