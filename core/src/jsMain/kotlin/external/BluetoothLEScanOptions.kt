package com.juul.kable.external

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
internal external interface BluetoothLEScanOptions {
    var filters: Array<BluetoothLEScanFilterInit>?
    var keepRepeatedDevices: Boolean?
    var acceptAllAdvertisements: Boolean?
}
