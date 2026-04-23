package com.juul.kable.external

import web.events.EventTarget
import kotlin.js.JsArray
import kotlin.js.JsBoolean
import kotlin.js.Promise

/** https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth */
internal abstract external class Bluetooth : EventTarget {
    fun getAvailability(): Promise<JsBoolean>
    fun requestDevice(options: RequestDeviceOptions): Promise<BluetoothDevice>
    fun requestLEScan(options: BluetoothLEScanOptions): Promise<BluetoothScan>
    fun getDevices(): Promise<JsArray<BluetoothDevice>>
}
