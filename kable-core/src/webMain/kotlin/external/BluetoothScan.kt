package com.juul.kable.external

import kotlin.js.JsAny

/**
 * https://webbluetoothcg.github.io/web-bluetooth/scanning.html#bluetoothlescan
 */
internal external interface BluetoothScan : JsAny {
    fun stop()
}
