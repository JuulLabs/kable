package com.juul.kable.external

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/BluetoothCharacteristicProperties
 * https://webbluetoothcg.github.io/web-bluetooth/#characteristicproperties-interface
 */
internal external interface BluetoothCharacteristicProperties {

    /** Returns a boolean that is `true` if signed writing to the characteristic value is permitted. */
    val authenticatedSignedWrites: Boolean

    /** Returns a boolean that is `true` if the broadcast of the characteristic value is permitted using the Server Characteristic Configuration Descriptor. */
    val broadcast: Boolean

    /** Returns a boolean that is `true` if indications of the characteristic value with acknowledgement is permitted. */
    val indicate: Boolean

    /** Returns a boolean that is `true` if notifications of the characteristic value without acknowledgement is permitted. */
    val notify: Boolean

    /** Returns a boolean that is `true` if the reading of the characteristic value is permitted. */
    val read: Boolean

    /** Returns a boolean that is `true` if reliable writes to the characteristic is permitted. */
    val reliableWrite: Boolean

    /** Returns a boolean that is `true` if reliable writes to the characteristic descriptor is permitted. */
    val writableAuxiliaries: Boolean

    /** Returns a boolean that is `true` if the writing to the characteristic with response is permitted. */
    val write: Boolean

    /** Returns a boolean that is `true` if the writing to the characteristic without response is permitted. */
    val writeWithoutResponse: Boolean
}
