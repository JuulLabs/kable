package com.juul.kable.external

import js.collections.JsMap
import kotlin.js.JsArray
import kotlin.js.JsNumber
import kotlin.js.JsString
import org.khronos.webgl.DataView
import org.w3c.dom.events.Event

internal typealias BluetoothManufacturerDataMap = JsMap<JsNumber, DataView>

/**
 * https://webbluetoothcg.github.io/web-bluetooth/#bluetoothadvertisingevent
 */
internal abstract external class BluetoothAdvertisingEvent : Event {
    val device: BluetoothDevice
    val uuids: JsArray<JsString>
    val name: String?
    val rssi: Int?
    val txPower: Int?
    val manufacturerData: BluetoothManufacturerDataMap
    val serviceData: BluetoothServiceDataMap
}
