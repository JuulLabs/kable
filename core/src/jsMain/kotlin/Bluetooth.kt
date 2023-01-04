package com.juul.kable

import com.juul.kable.Bluetooth.Availability.Available
import com.juul.kable.Bluetooth.Availability.Unavailable
import com.juul.kable.Options.Filter.Name
import com.juul.kable.Options.Filter.NamePrefix
import com.juul.kable.Options.Filter.Services
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.js.Promise
import com.juul.kable.external.Bluetooth as JsBluetooth

public actual enum class Reason

private const val AVAILABILITY_CHANGED = "availabilitychanged"

internal actual val bluetoothAvailability: Flow<Bluetooth.Availability> = callbackFlow {
    // https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/onavailabilitychanged
    val listener: (dynamic) -> Unit = { event ->
        val isAvailable = event.value as Boolean
        trySend(if (isAvailable) Available else Unavailable(reason = null))
    }

    bluetooth.apply {
        addEventListener(AVAILABILITY_CHANGED, listener)
        awaitClose {
            removeEventListener(AVAILABILITY_CHANGED, listener)
        }
    }
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
    .requestDevice(options.toDynamic())
    .then { device -> peripheral(device, builderAction) }

/**
 * Converts [Options] to JavaScript friendly object.
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
 *
 * _Note: Web BLE has a limitation that requires all UUIDS to be lowercase so we enforce that here._
 */
private fun Options.toDynamic(): dynamic = if (filters == null) {
    jso {
        this.acceptAllDevices = true
        this.optionalServices = optionalServices.lowercase()
    }
} else {
    jso {
        this.optionalServices = optionalServices.lowercase()
        this.filters = filters.map { it.toDynamic() }.toTypedArray()
    }
}

private fun Options.Filter.toDynamic(): dynamic =
    when (this) {
        is Name -> jso { this.name = name }
        is NamePrefix -> jso { this.namePrefix = namePrefix }
        is Services -> jso { this.services = services.lowercase() }
    }

private fun Array<String>.lowercase(): Array<String> = map { it.lowercase() }.toTypedArray()
