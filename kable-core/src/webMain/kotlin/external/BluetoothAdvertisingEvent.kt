package com.juul.kable.external

import js.collections.JsMap
import org.khronos.webgl.DataView
import web.events.Event
import kotlin.js.JsArray
import kotlin.js.JsNumber
import kotlin.js.JsString

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
