package com.juul.kable.external

import kotlin.js.JsAny
import kotlin.js.JsArray

/**
 * ```
 * dictionary BluetoothLEScanFilterInit {
 *   sequence<BluetoothServiceUUID> services;
 *   DOMString name;
 *   DOMString namePrefix;
 *   sequence<BluetoothManufacturerDataFilterInit> manufacturerData;
 *   sequence<BluetoothServiceDataFilterInit> serviceData;
 * };
 * ```
 *
 * https://webbluetoothcg.github.io/web-bluetooth/#device-discovery
 */
internal external interface BluetoothLEScanFilterInit : JsAny {
    var services: JsArray<BluetoothServiceUUID>?
    var name: String?
    var namePrefix: String?
    var manufacturerData: JsArray<BluetoothManufacturerDataFilterInit>?
    var serviceData: JsArray<BluetoothServiceDataFilterInit>?
}
