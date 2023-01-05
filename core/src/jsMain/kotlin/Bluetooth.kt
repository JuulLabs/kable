package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.Bluetooth
import com.juul.kable.external.BluetoothServiceUUID
import com.juul.kable.external.RequestDeviceOptions
import kotlinx.coroutines.CoroutineScope
import kotlin.js.Promise

// Deliberately NOT cast `as Bluetooth` to avoid potential class name collisions.
@Suppress("UnsafeCastFromDynamic")
internal val bluetooth: Bluetooth
    get() = checkNotNull(safeWebBluetooth) { "Bluetooth unavailable" }

// In a node build environment (e.g. unit test) there is no window, guard for that to avoid build errors.
private val safeWebBluetooth: dynamic =
    js("typeof(window) !== 'undefined' && window.navigator.bluetooth")

public fun CoroutineScope.requestPeripheral(
    options: Options,
    builderAction: PeripheralBuilderAction = {},
): Promise<Peripheral> = bluetooth
    .requestDevice(options.toRequestDeviceOptions())
    .then { device -> peripheral(device, builderAction) }

/**
 * Convert public API type to external Web Bluetooth (JavaScript) type.
 *
 * According to the `requestDevice`
 * [example](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/requestDevice#example), the form of the
 * JavaScript object should be similar to:
 * ```
 * let options = {
 *   filters: [
 *     {services: ['heart_rate']},
 *     {services: [0x1802, 0x1803]},
 *     {services: ['c48e6067-5295-48d3-8d5c-0395f61792b1']},
 *     {name: 'ExampleName'},
 *     {namePrefix: 'Prefix'}
 *   ],
 *   optionalServices: ['battery_service']
 * }
 * ```
 */
private fun Options.toRequestDeviceOptions(): RequestDeviceOptions = jso {
    if (this@toRequestDeviceOptions.filters.isNullOrEmpty()) {
        acceptAllDevices = true
    } else {
        filters = this@toRequestDeviceOptions.filters.toBluetoothLEScanFilterInit()
    }
    if (!this@toRequestDeviceOptions.optionalServices.isNullOrEmpty()) {
        optionalServices = this@toRequestDeviceOptions.optionalServices
            .map(Uuid::toBluetoothServiceUUID)
            .toTypedArray()
    }
}

// Note: Web Bluetooth requires that UUIDs be provided as lowercase strings.
internal fun Uuid.toBluetoothServiceUUID(): BluetoothServiceUUID = toString().lowercase()
