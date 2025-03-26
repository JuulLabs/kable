package com.juul.kable.external

import js.collections.JsMap
import org.khronos.webgl.DataView

/**
 * According to [Web Bluetooth](https://webbluetoothcg.github.io/web-bluetooth/#bluetoothservicedatamap):
 *
 * > Instances of `BluetoothServiceDataMap` have a `BackingMap` slot because they are maplike, which
 * > maps service UUIDs to the service’s data, converted to [DataView]s.
 */
internal typealias BluetoothServiceDataMap = JsMap<BluetoothServiceUUID, DataView>
