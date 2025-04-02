package com.juul.kable.external

import org.w3c.dom.events.Event

internal interface BluetoothManufacturerDataMap {
    fun entries(): JsIterator<Array<Any?>>
}

/**
 * https://webbluetoothcg.github.io/web-bluetooth/#bluetoothadvertisingevent
 */
internal abstract external class BluetoothAdvertisingEvent : Event {
    val device: BluetoothDevice
    val uuids: Array<String>
    val name: String?
    val rssi: Int?
    val txPower: Int?
    val manufacturerData: BluetoothManufacturerDataMap
    val serviceData: BluetoothServiceDataMap
}
