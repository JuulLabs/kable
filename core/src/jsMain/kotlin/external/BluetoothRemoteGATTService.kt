package com.juul.kable.external

import com.juul.kable.UUID
import org.w3c.dom.events.EventTarget
import kotlin.js.Promise

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/BluetoothRemoteGATTService
 * https://webbluetoothcg.github.io/web-bluetooth/#bluetoothremotegattservice
 */
internal abstract external class BluetoothRemoteGATTService : EventTarget {

    val uuid: UUID

    fun getCharacteristics(): Promise<Array<BluetoothRemoteGATTCharacteristic>>
}
