package com.juul.kable

import com.juul.kable.Bluetooth.Availability.Available
import com.juul.kable.Bluetooth.Availability.Unavailable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
