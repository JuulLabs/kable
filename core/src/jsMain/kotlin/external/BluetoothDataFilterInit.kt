package com.juul.kable.external

/**
 * ```
 * dictionary BluetoothDataFilterInit {
 *   BufferSource dataPrefix;
 *   BufferSource mask;
 * };
 * ```
 *
 * https://webbluetoothcg.github.io/web-bluetooth/#device-discovery
 */
internal abstract external class BluetoothDataFilterInit {
    var dataPrefix: BufferSource?
    var mask: BufferSource?
}
