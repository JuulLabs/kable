package com.juul.kable

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages observations for the specified [peripheral].
 *
 * The [characteristicChanges] property is expected to be fed with all characteristic changes associated with the
 * [peripheral]. The changes are then fanned out to individual [Flow]s created via [acquire] (associated with a specific
 * characteristic).
 *
 * For example, if you have a sequence of characteristic changes represented by characteristic A, B and C with their
 * corresponding change uniquely identified by a change number postfix (in other words: characteristic A emitting 3
 * different changes would be represented as A1, A2 and A3):
 *
 * ```
 *                                                       .--- acquire(A) --> A1, A2, A3
 *                             .----------------------. /
 *  A1, B1, C1, A2, A3, B2 --> | characteristicChange | ----- acquire(B) --> B1, B2
 *                             '----------------------' \
 *                                                       '--- acquire(C) --> C1
 * ```
 *
 * @param peripheral to perform notification actions against to enable/disable the observations.
 */
internal class Observers(
    private val peripheral: AndroidPeripheral,
) {

    val characteristicChanges = MutableSharedFlow<CharacteristicChange>()

    private val observers = HashMap<Characteristic, Int>()
    private val lock = Mutex()

    fun acquire(characteristic: Characteristic) = flow {
        try {
            characteristicChanges
                .onSubscription {
                    peripheral.suspendUntilReady()

                    if (observers.incrementAndGet(characteristic) == 1) {
                        peripheral.startObservation(characteristic)
                    }
                }
                .collect {
                    if (it.characteristic.characteristicUuid == characteristic.characteristicUuid &&
                        it.characteristic.serviceUuid == characteristic.serviceUuid
                    ) emit(it.data)
                }
        } finally {
            if (observers.decrementAndGet(characteristic) < 1) {
                try {
                    peripheral.stopObservation(characteristic)
                } catch (e: NotReadyException) {
                    // Silently ignore as it is assumed that failure is due to connection drop, in which case Android
                    // will clear the notifications.
                    Log.d(TAG, "Stop notification failure ignored.")
                }
            }
        }
    }

    suspend fun rewire() {
        lock.withLock {
            observers.keys.forEach { characteristic ->
                peripheral.startObservation(characteristic)
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
