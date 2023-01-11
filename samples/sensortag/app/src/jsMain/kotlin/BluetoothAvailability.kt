package com.juul.sensortag

import com.juul.kable.Bluetooth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

typealias AvailabilityListener = (availability: String) -> Unit

@JsExport
class BluetoothAvailability internal constructor(
    private val availability: Flow<Bluetooth.Availability>,
) {

    private val listeners = mutableListOf<AvailabilityListener>()

    internal fun launchIn(scope: CoroutineScope) {
        availability
            .map(Bluetooth.Availability::toString)
            .onEach { availability ->
                listeners.forEach { listener -> listener.invoke(availability) }
            }.launchIn(scope)
    }

    fun addListener(listener: AvailabilityListener) { listeners += listener }
    fun removeListener(listener: AvailabilityListener) { listeners -= listener }
}
