package com.juul.kable.external

import org.w3c.dom.events.EventTarget

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/BluetoothDevice
 * https://webbluetoothcg.github.io/web-bluetooth/#bluetoothdevice-interface
 */
internal abstract external class BluetoothDevice : EventTarget {
    val id: String
    val name: String?
    val gatt: BluetoothRemoteGATTServer?
}

internal fun BluetoothDevice.string(): String =
    "BluetoothDevice(id=$id, name=$name, gatt=${gatt?.string()})"
