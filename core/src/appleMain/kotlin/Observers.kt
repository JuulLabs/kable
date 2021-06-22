package com.juul.kable

import co.touchlab.stately.isolate.IsolateState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import platform.Foundation.NSData
import platform.Foundation.NSLog

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

    private val observations = Observations()

    fun acquire(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<NSData> {
        val cbCharacteristicUuid = characteristic.characteristicUuid.toCBUUID()
        val cbServiceUuid = characteristic.serviceUuid.toCBUUID()

        return characteristicChanges
            .onSubscription {
                peripheral.suspendUntilReady()
                if (observations.add(characteristic, onSubscription) == 1) {
                    peripheral.startNotifications(characteristic)
                }
                onSubscription()
            }
            .filter {
                it.cbCharacteristic.UUID == cbCharacteristicUuid &&
                    it.cbCharacteristic.service.UUID == cbServiceUuid
            }
            .map { it.data }
            .onCompletion {
                if (observations.remove(characteristic, onSubscription) < 1) {
                    try {
                        peripheral.stopNotifications(characteristic)
                    } catch (e: NotReadyException) {
                        // Silently ignore as it is assumed that failure is due to connection drop, in which case the
                        // system will clear the notifications.
                        NSLog("Stop notification failure ignored.")
                    }
                }
            }
    }

    suspend fun rewire() {
        observations.entries.forEach { (characteristic, observationStartedActions) ->
            peripheral.startNotifications(characteristic)
            observationStartedActions.forEach { it() }
        }
    }
}

private class Observations : IsolateState<MutableMap<Characteristic, MutableList<OnSubscriptionAction>>>(
    producer = { mutableMapOf() }
) {

    val entries: Map<Characteristic, List<OnSubscriptionAction>>
        get() = access {
            it.toMap()
        }

    fun add(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction
    ): Int = access {
        val actions = it[characteristic]
        if (actions == null) {
            val newActions = mutableListOf(onSubscription)
            it[characteristic] = newActions
            1
        } else {
            actions += onSubscription
            actions.count()
        }
    }

    fun remove(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction
    ): Int = access {
        val actions = it[characteristic]
        if (actions == null) {
            0
        } else {
            actions -= onSubscription
            actions.count()
        }
    }
}
