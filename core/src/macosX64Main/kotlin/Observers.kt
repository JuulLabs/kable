package com.juul.kable

import co.touchlab.stately.isolate.IsolateState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import platform.Foundation.NSData

/**
 * Manages observations for the specified [peripheral].
 *
 * The [characteristicChanges] property is expected to be fed with all characteristic changes associated with the
 * [peripheral]. The changes are then fanned-out to individual [Flow]s created via [acquire] (associated with a specific
 * characteristic).
 *
 * For example, if you have a sequence of characteristic changes represented by characteristic A, B and C with their
 * corresponding change uniquely identified by a sequence number postfix (in other words: characteristic A emitting 3
 * different changes would be represented as A1, A2 and A3):
 *
 * ```
 *                                                        .--- acquire(A) --> A1, A2, A3
 *                             .-----------------------. /
 *  A1, B1, C1, A2, A3, B2 --> | characteristicChanges | ----- acquire(B) --> B1, B2
 *                             '-----------------------' \
 *                                                        '--- acquire(C) --> C1
 * ```
 *
 * @param peripheral to perform notification actions against to enable/disable the observations.
 */
internal class Observers(
    private val peripheral: ApplePeripheral,
) {

    val characteristicChanges =
        MutableSharedFlow<PeripheralDelegate.DidUpdateValueForCharacteristic.Data>()

    private val observers = ObservationCount()

    fun acquire(characteristic: Characteristic): Flow<NSData> = flow {
        peripheral.suspendUntilReady()

        val cbCharacteristicUuid = characteristic.characteristicUuid.toCBUUID()
        val cbServiceUuid = characteristic.serviceUuid.toCBUUID()

        if (observers.incrementAndGet(characteristic) == 1) {
            peripheral.startNotifications(characteristic)
        }

        try {
            characteristicChanges.collect {
                if (it.cbCharacteristic.UUID == cbCharacteristicUuid &&
                    it.cbCharacteristic.service.UUID == cbServiceUuid
                ) emit(it.data)
            }
        } finally {
            if (observers.decrementAndGet(characteristic) < 1) {
                peripheral.stopNotifications(characteristic)
            }
        }
    }

    suspend fun rewire() {
        observers.keys.forEach { characteristic ->
            peripheral.startNotifications(characteristic)
        }
    }
}

private class ObservationCount : IsolateState<MutableMap<Characteristic, Int>>({ mutableMapOf() }) {

    val keys: Set<Characteristic>
        get() = access { it.keys.toSet() }

    fun incrementAndGet(characteristic: Characteristic): Int = access {
        val newValue = (it[characteristic] ?: 0) + 1
        it[characteristic] = newValue
        newValue
    }

    fun decrementAndGet(characteristic: Characteristic): Int = access {
        val newValue = (it[characteristic] ?: 0) - 1
        if (newValue < 1) it -= characteristic else it[characteristic] = newValue
        newValue
    }
}
