package com.juul.kable.external

import web.events.EventTarget
import kotlin.js.Promise

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/BluetoothDevice
 * https://webbluetoothcg.github.io/web-bluetooth/#bluetoothdevice-interface
 */
internal abstract external class BluetoothDevice : EventTarget {
    val id: String
    val name: String?

    /**
     * Non-`null` when:
     *
     * > [..] "bluetooth"'s extra permission data for `this`'s relevant settings object has an
     * > `AllowedBluetoothDevice` _allowedDevice_ in its `allowedDevices` list with
     * > `allowedDevice.device` the same device as `this.representedDevice` and
     * > `allowedDevice.mayUseGATT` equal to `true` [..]
     *
     * https://webbluetoothcg.github.io/web-bluetooth/#bluetoothdevice-interface
     */
    val gatt: BluetoothRemoteGATTServer?

    // Experimental advertisement features
    // https://webbluetoothcg.github.io/web-bluetooth/#dom-bluetoothdevice-watchadvertisements
    // Requires chrome://flags/#enable-experimental-web-platform-features
    fun watchAdvertisements(): Promise<Nothing?>
    fun unwatchAdvertisements(): Promise<Nothing?>
    val watchingAdvertisements: Boolean
}

internal fun BluetoothDevice.string(): String =
    "BluetoothDevice(id=$id, name=$name, gatt=${gatt?.string()})"
