package com.juul.kable

import com.juul.kable.external.BluetoothRemoteGATTCharacteristic
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.khronos.webgl.DataView
import org.w3c.dom.events.Event

private typealias EventListener = (Event) -> Unit

private const val CHARACTERISTIC_VALUE_CHANGED = "characteristicvaluechanged"

private data class CharacteristicChange(
    val characteristic: Characteristic,
    val data: DataView,
)

private data class Observation(
    var count: Int = 0,
    var listener: EventListener?,
)

internal class Observers(
    private val peripheral: Peripheral
) {

    private val observers = mutableMapOf<Characteristic, Observation>()
    private val changes = MutableSharedFlow<CharacteristicChange>(extraBufferCapacity = 64)

    fun acquire(characteristic: Characteristic) = flow {
        val bluetoothRemoteGATTCharacteristic =
            peripheral.bluetoothRemoteGATTCharacteristicFrom(characteristic)
        val observation = observers[characteristic] ?: run {
            Observation(listener = characteristic.createListener()).also {
                observers[characteristic] = it
            }
        }

        if (++observation.count == 1) {
            bluetoothRemoteGATTCharacteristic.apply {
                addEventListener(CHARACTERISTIC_VALUE_CHANGED, observation.listener)
                startNotifications().await()
            }
        }

        try {
            changes.collect {
                if (it.characteristic.characteristicUuid == characteristic.characteristicUuid) {
                    emit(it.data)
                }
            }
        } finally {
            if (--observation.count < 1) {
                bluetoothRemoteGATTCharacteristic.apply {
                    /* Throws `DOMException` if connection is closed:
                     *
                     * DOMException: Failed to execute 'stopNotifications' on 'BluetoothRemoteGATTCharacteristic':
                     * Characteristic with UUID [...] is no longer valid. Remember to retrieve the characteristic
                     * again after reconnecting.
                     *
                     * Wrapped in `runCatching` to silently ignore failure, as notification will already be
                     * invalidated due to the connection being closed.
                     */
                    runCatching { stopNotifications().await() }

                    removeEventListener(CHARACTERISTIC_VALUE_CHANGED, observation.listener)
                }
                observers.remove(characteristic)
            }
        }
    }

    fun invalidate() {
        observers.forEach { (_, observation) ->
            observation.listener = null
        }
    }

    suspend fun rewire(services: List<PlatformService>) {
        if (observers.isEmpty()) return
        console.log("Rewiring observers")

        observers.forEach { (identifier, _) ->
            val characteristic =
                services.first { it.serviceUuid == identifier.serviceUuid }
                    .characteristics.first { it.characteristicUuid == identifier.characteristicUuid }

            console.log("Starting notifications for $identifier")
            characteristic
                .bluetoothRemoteGATTCharacteristic
                .apply {
                    startNotifications().await()
                    addEventListener(CHARACTERISTIC_VALUE_CHANGED, characteristic.createListener())
                }
        }
    }

    fun clear() {
        observers.clear()
    }

    private fun Characteristic.createListener(): EventListener = { event ->
        val target = event.target as BluetoothRemoteGATTCharacteristic
        changes.tryEmit(CharacteristicChange(this, target.value!!))
    }
}
