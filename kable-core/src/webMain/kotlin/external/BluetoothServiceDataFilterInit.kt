package com.juul.kable.external

/**
 * ```
 * dictionary BluetoothServiceDataFilterInit : BluetoothDataFilterInit {
 *   required BluetoothServiceUUID service;
 * };
 * ```
 *
 * https://webbluetoothcg.github.io/web-bluetooth/#device-discovery
 */
internal external interface BluetoothServiceDataFilterInit : BluetoothDataFilterInit {
    var service: BluetoothServiceUUID
}
