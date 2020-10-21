package com.juul.kable.external

import com.juul.kable.UUID
import org.khronos.webgl.BufferDataSource
import org.khronos.webgl.DataView
import org.w3c.dom.events.EventTarget
import kotlin.js.Promise

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/BluetoothRemoteGATTDescriptor
 * https://webbluetoothcg.github.io/web-bluetooth/#bluetoothremotegattdescriptor
 */
internal abstract external class BluetoothRemoteGATTDescriptor : EventTarget {
    val uuid: UUID
    fun readValue(): Promise<DataView>
    fun writeValue(value: BufferSource): Promise<Unit>
}
