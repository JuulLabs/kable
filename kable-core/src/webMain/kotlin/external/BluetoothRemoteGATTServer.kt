package com.juul.kable.external

import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.Promise

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/BluetoothRemoteGATTServer
 * https://webbluetoothcg.github.io/web-bluetooth/#bluetoothgattremoteserver-interface
 */
internal external interface BluetoothRemoteGATTServer : JsAny {

    val device: BluetoothDevice
    val connected: Boolean

    fun connect(): Promise<BluetoothRemoteGATTServer>
    fun disconnect(): Unit

    fun getPrimaryServices(): Promise<JsArray<BluetoothRemoteGATTService>>

    fun getPrimaryServices(
        service: BluetoothServiceUUID,
    ): Promise<JsArray<BluetoothRemoteGATTService>>
}

internal fun BluetoothRemoteGATTServer.string() =
    "BluetoothRemoteGATTServer(connected=$connected)"
