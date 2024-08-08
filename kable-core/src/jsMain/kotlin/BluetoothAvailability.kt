package com.juul.kable

import com.juul.kable.AvailabilityReason.BluetoothUndefined
import com.juul.kable.Bluetooth.Availability.Available
import com.juul.kable.Bluetooth.Availability.Unavailable
import com.juul.kable.external.BluetoothAvailabilityChanged
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import org.w3c.dom.events.Event

public actual enum class AvailabilityReason {
    /** `window.navigator.bluetooth` is undefined. */
    BluetoothUndefined,
}

private const val AVAILABILITY_CHANGED = "availabilitychanged"

internal actual val bluetoothAvailability: Flow<Bluetooth.Availability> =
    bluetoothOrNull()?.let { bluetooth ->
        callbackFlow {
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
            val isAvailable = bluetooth.getAvailability().await()
            val availability = if (isAvailable) Available else Unavailable(reason = null)
            emit(availability)
        }
    } ?: flowOf(Unavailable(reason = BluetoothUndefined))
