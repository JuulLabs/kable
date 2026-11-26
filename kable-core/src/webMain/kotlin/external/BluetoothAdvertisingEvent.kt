package com.juul.kable.external

import js.collections.JsMap
import js.objects.unsafeJso
import org.khronos.webgl.DataView
import web.events.Event
import web.events.EventTarget
import web.events.EventType
import web.events.HasTargets
import web.events.addEventHandler
import kotlin.js.JsArray
import kotlin.js.JsNumber
import kotlin.js.JsString

internal typealias BluetoothManufacturerDataMap = JsMap<JsNumber, DataView>

internal val ADVERTISEMENT_RECEIVED = EventType<BluetoothAdvertisingEvent>("advertisementreceived")

internal fun EventTarget.addAdvertisementReceivedHandler(
    once: Boolean = false,
    handler: (BluetoothAdvertisingEvent) -> Unit,
): () -> Unit = addEventHandler(ADVERTISEMENT_RECEIVED, unsafeJso { this.once = once }, handler)

/**
 * https://webbluetoothcg.github.io/web-bluetooth/#bluetoothadvertisingevent
 */
internal abstract external class BluetoothAdvertisingEvent : Event, HasTargets<EventTarget, EventTarget> {
    override val currentTarget: EventTarget
    override val target: EventTarget
    val device: BluetoothDevice
    val uuids: JsArray<JsString>
    val name: String?
    val rssi: Int?
    val txPower: Int?
    val manufacturerData: BluetoothManufacturerDataMap
    val serviceData: BluetoothServiceDataMap
}
