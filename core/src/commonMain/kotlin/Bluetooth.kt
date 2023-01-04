package com.juul.kable

import kotlinx.coroutines.flow.Flow

public expect enum class Reason

public object Bluetooth {

    public sealed class Availability {

        public object Available : Availability() {
            override fun toString(): String = "Available"
        }

        public data class Unavailable(val reason: Reason?) : Availability()
    }

    public val availability: Flow<Availability> = bluetoothAvailability
}

internal expect val bluetoothAvailability: Flow<Bluetooth.Availability>
