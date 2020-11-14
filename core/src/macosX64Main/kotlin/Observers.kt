package com.juul.kable

import co.touchlab.stately.isolate.IsolateState
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
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
    private val peripheral: ApplePeripheral,
) {

    // todo: MutableSharedFlow when Coroutines 1.4.x-mt is released.
    val characteristicChanges =
        BroadcastChannel<PeripheralDelegate.DidUpdateValueForCharacteristic.Data>(1)

    private val observers = ObservationCount()

    fun acquire(characteristic: Characteristic): Flow<NSData> = flow {
        println("Observers acquire suspendUntilReady")
        peripheral.suspendUntilReady()
        println("Observers acquire suspendUntilReady DONE")

        val cbCharacteristicUuid = characteristic.characteristicUuid.toCBUUID()
        val cbServiceUuid = characteristic.serviceUuid.toCBUUID()

        if (observers.incrementAndGet(characteristic) == 1) {
            println("Observers acquire startNotifications")
            peripheral.startNotifications(characteristic)
            println("Observers acquire startNotifications DONE")
        } else {
            println("Observers acquire startNotifications SKIPPED")
        }

        try {
            characteristicChanges.consumeEach {
                if (it.cbCharacteristic.UUID == cbCharacteristicUuid &&
                    it.cbCharacteristic.service.UUID == cbServiceUuid
                ) emit(it.data)
            }
        } catch (t: Throwable) {
            println("Observers acquire caught $t")
            throw t
        } finally {
            println("Observers acquire finally")
            if (observers.decrementAndGet(characteristic) < 1) {
                peripheral.stopNotifications(characteristic)
            }
            println("Observers acquire finally DONE")
        }
    }

    suspend fun rewire() {
        println("Observers rewire")
        observers.keys.forEach { characteristic ->
            peripheral.startNotifications(characteristic)
        }
        println("Observers rewire DONE")
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
