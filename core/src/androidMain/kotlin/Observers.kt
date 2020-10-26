package com.juul.kable

import com.juul.kable.gatt.OnCharacteristicChanged
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class Observers(
    private val peripheral: Peripheral,
) {

    val characteristicChanges = MutableSharedFlow<OnCharacteristicChanged>(extraBufferCapacity = 64)

    private val observers = HashMap<Characteristic, Int>()

    fun acquire(characteristic: Characteristic) = flow {
        if (observers.incrementAndGet(characteristic) == 1) {
            peripheral.startNotifications(characteristic)
        }

        try {
            characteristicChanges.collect {
                if (it.characteristic.uuid == characteristic.characteristicUuid &&
                    it.characteristic.instanceId == characteristic.instanceId
                ) emit(it.value)
            }
        } finally {
            if (observers.decrementAndGet(characteristic) < 1) {
                peripheral.stopNotifications(characteristic)
            }
        }
    }

    suspend fun invalidate() {
        lock.withLock {
            observers.clear()
        }
    }

    private val lock = Mutex()

    suspend fun rewire() {
        if (observers.isEmpty()) return
        lock.withLock {
            observers.keys.forEach { characteristic ->
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
