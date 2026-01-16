package com.juul.kable.external

/**
 * ```
 * dictionary BluetoothManufacturerDataFilterInit : BluetoothDataFilterInit {
 *   required [EnforceRange] unsigned short companyIdentifier;
 * };
 * ```
 *
 * https://webbluetoothcg.github.io/web-bluetooth/#device-discovery
 */
internal external interface BluetoothManufacturerDataFilterInit : BluetoothDataFilterInit {
    var companyIdentifier: Int?
}
