package com.juul.kable

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class Observers(
    private val peripheral: AndroidPeripheral,
) {

    val characteristicChanges = MutableSharedFlow<CharacteristicChange>()

    private val observers = HashMap<Characteristic, Int>()
    private val lock = Mutex()

    fun acquire(characteristic: Characteristic) = flow {
        peripheral.suspendUntilReady()

        if (observers.incrementAndGet(characteristic) == 1) {
            peripheral.startNotifications(characteristic)
        }

        try {
            characteristicChanges.collect {
                if (it.characteristic.characteristicUuid == characteristic.characteristicUuid &&
                    it.characteristic.serviceUuid == characteristic.serviceUuid
                ) emit(it.data)
            }
        } finally {
            if (observers.decrementAndGet(characteristic) < 1) {
                peripheral.stopNotifications(characteristic)
            }
        }
    }

    suspend fun rewire() {
        lock.withLock {
            observers.keys.forEach { characteristic ->
                peripheral.startNotifications(characteristic)
            }
        }
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

private suspend fun Peripheral.suspendUntilReady() {
    events.first { it == Event.Ready }
}
