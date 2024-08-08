package com.juul.kable

import kotlinx.coroutines.flow.Flow

public actual enum class AvailabilityReason {
    // Not implemented.
}

internal actual val bluetoothAvailability: Flow<Bluetooth.Availability> = jvmNotImplementedException()
