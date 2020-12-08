package com.juul.kable.external

import org.w3c.dom.events.EventTarget
import kotlin.js.Promise

/** https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth */
internal abstract external class Bluetooth: EventTarget {
    fun requestDevice(options: dynamic): Promise<BluetoothDevice>
    fun requestLEScan(options: dynamic): Promise<BluetoothScan>
}
