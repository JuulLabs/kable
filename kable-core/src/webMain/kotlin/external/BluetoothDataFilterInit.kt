package com.juul.kable.external

import kotlin.js.JsAny

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
internal external interface BluetoothDataFilterInit : JsAny {
    var dataPrefix: BufferSource?
    var mask: BufferSource?
}
