package com.juul.kable.external

import kotlin.js.JsAny
import kotlin.js.JsArray

/**
 * ```
 * dictionary BluetoothLEScanOptions {
 *   sequence<BluetoothLEScanFilterInit> filters;
 *   boolean keepRepeatedDevices = false;
 *   boolean acceptAllAdvertisements = false;
 * };
 * ```
 *
 * https://webbluetoothcg.github.io/web-bluetooth/scanning.html#scanning
 */
internal external interface BluetoothLEScanOptions : JsAny {
    var filters: JsArray<BluetoothLEScanFilterInit>?
    var keepRepeatedDevices: Boolean?
    var acceptAllAdvertisements: Boolean?
}
