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
internal external class BluetoothManufacturerDataFilterInit : BluetoothDataFilterInit {
    var companyIdentifier: Int?
}
