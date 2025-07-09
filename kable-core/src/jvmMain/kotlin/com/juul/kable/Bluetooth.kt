package com.juul.kable

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public actual enum class Reason {
    // Not implemented.
}

// This is not a proper implementation, but this property is deprecated, so...
internal actual val bluetoothAvailability: Flow<Bluetooth.Availability> = flowOf(Bluetooth.Availability.Available)
