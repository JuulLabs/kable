package com.juul.kable.external

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
internal external interface RequestDeviceOptions {
    var filters: Array<BluetoothLEScanFilterInit>?
    var optionalServices: Array<BluetoothServiceUUID>?
    var optionalManufacturerData: ByteArray?
    var acceptAllDevices: Boolean?
}
