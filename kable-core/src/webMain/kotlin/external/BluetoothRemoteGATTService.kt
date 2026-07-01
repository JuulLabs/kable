package com.juul.kable.external

import com.juul.kable.UUID
import web.events.EventTarget
import kotlin.js.JsArray
import kotlin.js.Promise

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/BluetoothRemoteGATTService
 * https://webbluetoothcg.github.io/web-bluetooth/#bluetoothremotegattservice
 */
internal external class BluetoothRemoteGATTService : EventTarget {

    val uuid: UUID

    fun getCharacteristics(): Promise<JsArray<BluetoothRemoteGATTCharacteristic>>
}
