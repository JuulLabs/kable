package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.Bluetooth.Availability.Available
import com.juul.kable.Bluetooth.Availability.Unavailable
import com.juul.kable.Reason.BluetoothUndefined
import com.juul.kable.external.BluetoothAvailabilityChanged
import com.juul.kable.external.BluetoothLEScanFilterInit
import com.juul.kable.external.BluetoothServiceUUID
import com.juul.kable.external.RequestDeviceOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import org.w3c.dom.events.Event
import kotlin.js.Promise
import com.juul.kable.external.Bluetooth as JsBluetooth

public actual enum class Reason {
    /** `window.navigator.bluetooth` is undefined. */
    BluetoothUndefined,
}

private const val AVAILABILITY_CHANGED = "availabilitychanged"

internal actual val bluetoothAvailability: Flow<Bluetooth.Availability> = callbackFlow {
    if (safeWebBluetooth == null) return@callbackFlow

    // https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/onavailabilitychanged
    val listener: (Event) -> Unit = { event ->
        val isAvailable = event.unsafeCast<BluetoothAvailabilityChanged>().value
        trySend(if (isAvailable) Available else Unavailable(reason = null))
    }

    bluetooth.apply {
        addEventListener(AVAILABILITY_CHANGED, listener)
        awaitClose {
            removeEventListener(AVAILABILITY_CHANGED, listener)
        }
    }
}.onStart {
    val availability = if (safeWebBluetooth == null) {
        Unavailable(reason = BluetoothUndefined)
    } else {
        val isAvailable = bluetooth.getAvailability().await()
        if (isAvailable) Available else Unavailable(reason = null)
    }
    emit(availability)
}

// Deliberately NOT cast `as Bluetooth` to avoid potential class name collisions.
@Suppress("UnsafeCastFromDynamic")
internal val bluetooth: JsBluetooth
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
    val jsFilters = this@toRequestDeviceOptions.filters()
    if (jsFilters.isNotEmpty()) {
        filters = jsFilters.toTypedArray()
    } else {
        acceptAllDevices = true
    }
    if (!this@toRequestDeviceOptions.optionalServices.isNullOrEmpty()) {
        optionalServices = this@toRequestDeviceOptions.optionalServices
            .map(Uuid::toBluetoothServiceUUID)
            .toTypedArray()
    }
}

private fun Options.filters(): List<BluetoothLEScanFilterInit> =
    when {
        filterSets?.isNotEmpty() == true -> filterSets.toBluetoothLEScanFilterInit()
        filters?.isNotEmpty() == true -> filters.toBluetoothLEScanFilterInit()
        else -> FilterPredicateSetBuilder().apply(predicates).build().toBluetoothLEScanFilterInit()
    }

// Note: Web Bluetooth requires that UUIDs be provided as lowercase strings.
internal fun Uuid.toBluetoothServiceUUID(): BluetoothServiceUUID = toString().lowercase()
