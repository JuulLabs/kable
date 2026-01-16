package com.juul.kable.external

import com.juul.kable.UUID
import kotlin.js.JsAny

/**
 * According to [Web Bluetooth](https://webbluetoothcg.github.io/web-bluetooth/#uuids):
 *
 * > Note: This standard provides the BluetoothUUID.canonicalUUID(alias) function to map
 * > a 16- or 32-bit Bluetooth UUID alias to its 128-bit form.
 *
 * _See also: [Standardized UUIDs](https://webbluetoothcg.github.io/web-bluetooth/#standardized-uuids)_
 */
internal abstract external class BluetoothUUID {
    internal companion object {
        internal fun canonicalUUID(alias: JsAny?): UUID
    }
}
