package com.juul.kable.external

import com.juul.kable.UUID
import kotlin.js.JsArray
import org.khronos.webgl.DataView
import org.w3c.dom.events.EventTarget
import kotlin.js.Promise

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/BluetoothRemoteGATTCharacteristic
 * https://webbluetoothcg.github.io/web-bluetooth/#bluetoothgattcharacteristic-interface
 */
internal external class BluetoothRemoteGATTCharacteristic : EventTarget {

    val service: BluetoothRemoteGATTService
    val uuid: UUID
    val properties: BluetoothCharacteristicProperties
    val value: DataView?

    fun getDescriptor(descriptor: BluetoothDescriptorUUID): Promise<BluetoothRemoteGATTDescriptor>
    fun getDescriptors(): Promise<JsArray<BluetoothRemoteGATTDescriptor>>

    fun readValue(): Promise<DataView>

    fun writeValueWithResponse(value: BufferSource): Promise<Nothing?>
    fun writeValueWithoutResponse(value: BufferSource): Promise<Nothing?>

    /**
     * > All notifications become inactive when a device is disconnected. A site that wants to keep
     * > getting notifications after reconnecting needs to call [startNotifications] again, and
     * > there is an unavoidable risk that some notifications will be missed in the gap before
     * > [startNotifications] takes effect.
     *
     * https://webbluetoothcg.github.io/web-bluetooth/#active-notification-context-set
     */
    fun startNotifications(): Promise<BluetoothRemoteGATTCharacteristic>

    fun stopNotifications(): Promise<BluetoothRemoteGATTCharacteristic>
}
