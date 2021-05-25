package com.juul.kable

import com.juul.kable.external.BluetoothRemoteGATTCharacteristic
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.sync.withLock
import org.khronos.webgl.DataView
import org.w3c.dom.events.Event

private typealias ObservationListener = (Event) -> Unit

private const val CHARACTERISTIC_VALUE_CHANGED = "characteristicvaluechanged"

private data class CharacteristicChange(
    val characteristic: Characteristic,
    val data: DataView,
)

private data class Observation(
    var count: Int = 0,
    var listener: ObservationListener?,
)

internal class Observers(
    private val peripheral: JsPeripheral
) {

    private val observers = mutableMapOf<Characteristic, Observation>()
    private val characteristicChanges =
        MutableSharedFlow<CharacteristicChange>(extraBufferCapacity = 64)

    fun acquire(characteristic: Characteristic): Flow<DataView> {
        lateinit var bluetoothRemoteGATTCharacteristic: BluetoothRemoteGATTCharacteristic
        lateinit var observation: Observation

        return characteristicChanges
            .onSubscription {
                peripheral.suspendUntilReady()

                bluetoothRemoteGATTCharacteristic =
                    peripheral.bluetoothRemoteGATTCharacteristicFrom(characteristic)

                observation = observers[characteristic] ?: run {
                    Observation(listener = characteristic.createListener()).also {
                        observers[characteristic] = it
                    }
                }

                if (++observation.count == 1) {
                    bluetoothRemoteGATTCharacteristic.apply {
                        addEventListener(CHARACTERISTIC_VALUE_CHANGED, observation.listener)
                        peripheral.ioLock.withLock {
                            startNotifications().await()
                        }
                    }
                }
            }
            .onCompletion {
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
                        runCatching {
                            peripheral.ioLock.withLock {
                                stopNotifications().await()
                            }
                        }

                        removeEventListener(CHARACTERISTIC_VALUE_CHANGED, observation.listener)
                    }
                    observers.remove(characteristic)
                }
            }
            .filter {
                it.characteristic.characteristicUuid == characteristic.characteristicUuid
            }
            .map { it.data }
    }

    fun invalidate() {
        observers.forEach { (_, observation) ->
            observation.listener = null
        }
    }

    suspend fun rewire(services: List<PlatformService>) {
        if (observers.isEmpty()) return

        observers.forEach { (characteristic, _) ->
            val platformCharacteristic =
                services.first { it.serviceUuid == characteristic.serviceUuid }
                    .characteristics.first { it.characteristicUuid == characteristic.characteristicUuid }

            platformCharacteristic
                .bluetoothRemoteGATTCharacteristic
                .apply {
                    peripheral.ioLock.withLock {
                        startNotifications().await()
                    }
                    addEventListener(CHARACTERISTIC_VALUE_CHANGED, platformCharacteristic.createListener())
                }
        }
    }

    fun clear() {
        observers.clear()
    }

    private fun Characteristic.createListener(): ObservationListener = { event ->
        val target = event.target as BluetoothRemoteGATTCharacteristic
        characteristicChanges.tryEmit(CharacteristicChange(this, target.value!!))
    }
}
