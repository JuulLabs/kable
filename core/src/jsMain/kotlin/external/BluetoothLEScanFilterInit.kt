package com.juul.kable.external

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
internal external interface BluetoothLEScanFilterInit {
    var services: Array<BluetoothServiceUUID>?
    var name: String?
    var namePrefix: String?
    var manufacturerData: Array<BluetoothManufacturerDataFilterInit>?
    var serviceData: Array<BluetoothServiceDataFilterInit>?
}
