package com.juul.kable

import com.juul.kable.Bluetooth.State
import kotlinx.coroutines.flow.Flow

internal actual suspend fun isBluetoothSupported(): Boolean = jvmNotImplementedException()

internal actual val bluetoothState: Flow<State> = jvmNotImplementedException()

public actual enum class AvailabilityReason {
    // Not implemented.
}

internal actual val bluetoothAvailability: Flow<Bluetooth.Availability> = jvmNotImplementedException()
