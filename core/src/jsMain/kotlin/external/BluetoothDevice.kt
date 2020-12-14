package com.juul.kable.external

import org.w3c.dom.events.EventTarget
import kotlin.js.Promise

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/BluetoothDevice
 * https://webbluetoothcg.github.io/web-bluetooth/#bluetoothdevice-interface
 */
internal abstract external class BluetoothDevice : EventTarget {
    val id: String
    val name: String?
    val gatt: BluetoothRemoteGATTServer?

    // Experimental advertisement features
    // https://webbluetoothcg.github.io/web-bluetooth/#dom-bluetoothdevice-watchadvertisements
    // Requires chrome://flags/#enable-experimental-web-platform-features
    fun watchAdvertisements(): Promise<Unit>
    fun unwatchAdvertisements(): Promise<Unit>
    val watchingAdvertisements: Boolean
}

internal fun BluetoothDevice.string(): String =
    "BluetoothDevice(id=$id, name=$name, gatt=${gatt?.string()})"
