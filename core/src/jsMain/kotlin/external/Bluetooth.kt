package com.juul.kable.external

import kotlin.js.Promise

/** https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth */
internal external interface Bluetooth {
    fun requestDevice(options: dynamic): Promise<BluetoothDevice>
}
