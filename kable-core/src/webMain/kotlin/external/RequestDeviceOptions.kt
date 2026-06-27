package com.juul.kable.external

import org.khronos.webgl.Int8Array
import kotlin.js.JsAny
import kotlin.js.JsArray

/**
 * ```
 * dictionary RequestDeviceOptions {
 *   sequence<BluetoothLEScanFilterInit> filters;
 *   sequence<BluetoothServiceUUID> optionalServices = [];
 *   sequence<unsigned short> optionalManufacturerData = [];
 *   boolean acceptAllDevices = false;
 * };
 * ```
 *
 * https://webbluetoothcg.github.io/web-bluetooth/#device-discovery
 */
internal external interface RequestDeviceOptions : JsAny {
    var filters: JsArray<BluetoothLEScanFilterInit>?
    var optionalServices: JsArray<BluetoothServiceUUID>?
    var optionalManufacturerData: Int8Array?
    var acceptAllDevices: Boolean?
}
