package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.gatt.OnCharacteristicChanged
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private data class CharacteristicIdentifier(
    val serviceUuid: Uuid,
    val characteristicUuid: Uuid,
)

internal class Observers(
    private val peripheral: Peripheral,
) {

    val characteristicChanges = MutableSharedFlow<OnCharacteristicChanged>(extraBufferCapacity = 64)

    private val observers = HashMap<CharacteristicIdentifier, Int>()

    fun acquire(characteristic: Characteristic) = flow {
        val identifier = characteristic.identifier

        if (observers.incrementAndGet(identifier) == 1) {
            peripheral.startNotifications(characteristic)
        }

        try {
            characteristicChanges.collect {
                if (it.characteristic.uuid == characteristic.bluetoothGattCharacteristic.uuid &&
                    it.characteristic.instanceId == characteristic.bluetoothGattCharacteristic.instanceId
                ) emit(it.value)
            }
        } finally {
            if (observers.decrementAndGet(identifier) < 1) {
                peripheral.stopNotifications(characteristic)
            }
        }
    }

    fun invalidate() {
        observers.clear()
    }

    private val lock = Mutex()

    suspend fun rewire() {
        if (observers.isEmpty()) return
        val services = peripheral.services ?: error("Services unavailable for rewiring observers")

        lock.withLock {
            observers.keys.forEach { identifier ->
                val characteristic =
                    services.first { it.uuid == identifier.serviceUuid }
                        .characteristics.first { it.uuid == identifier.characteristicUuid }
                peripheral.startNotifications(characteristic)
            }
        }
    }

    fun clear() {
        observers.clear()
    }

    private suspend fun <K> MutableMap<K, Int>.incrementAndGet(
        key: K
    ) = lock.withLock {
        val newValue = (get(key) ?: 0) + 1
        put(key, newValue)
        newValue
    }

    private suspend fun <K> MutableMap<K, Int>.decrementAndGet(
        key: K
    ) = lock.withLock {
        val newValue = (get(key) ?: 0) - 1
        if (newValue < 1) remove(key) else put(key, newValue)
        newValue
    }
}

private val Characteristic.identifier: CharacteristicIdentifier
    get() = CharacteristicIdentifier(serviceUuid = serviceUuid, characteristicUuid = uuid)

private suspend fun Peripheral.startNotifications(
    characteristic: Characteristic
) {
    TODO()
    // todo: set notifications to true
    // todo: write descriptor
}

private suspend fun Peripheral.stopNotifications(
    characteristic: Characteristic
) {
    TODO()
    // todo: write descriptor
    // todo: set notifications to false
}
