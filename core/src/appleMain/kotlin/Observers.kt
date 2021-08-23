package com.juul.kable

import co.touchlab.stately.ensureNeverFrozen
import co.touchlab.stately.isolate.IsolateState
import com.juul.kable.logs.Logger
import com.juul.kable.logs.detail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import platform.Foundation.NSData
import kotlin.coroutines.cancellation.CancellationException

internal sealed class AppleObservationEvent {

    abstract val characteristic: Characteristic

    data class CharacteristicChange(
        override val characteristic: Characteristic,
        val data: NSData,
    ) : AppleObservationEvent()

    data class Error(
        override val characteristic: Characteristic,
        val cause: Throwable,
    ) : AppleObservationEvent()
}

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
    private val logger: Logger,
) {

    val characteristicChanges = MutableSharedFlow<AppleObservationEvent>()
    private val observations = Observations()

    fun acquire(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<NSData> {
        return characteristicChanges
            .onSubscription {
                peripheral.suspendUntilReady()
                if (observations.add(characteristic, onSubscription) == 1) {
                    peripheral.startNotifications(characteristic)
                }
                onSubscription()
            }
            .filter {
                it.characteristic.characteristicUuid == characteristic.characteristicUuid &&
                    it.characteristic.serviceUuid == characteristic.serviceUuid
            }
            .map {
                when (it) {
                    is AppleObservationEvent.Error -> throw it.cause
                    is AppleObservationEvent.CharacteristicChange -> it.data
                }
            }
            .onCompletion {
                if (observations.remove(characteristic, onSubscription) == 0) {
                    try {
                        peripheral.stopNotifications(characteristic)
                    } catch (e: NotReadyException) {
                        // Silently ignore as it is assumed that failure is due to connection drop, in which case the
                        // system will clear the notifications.
                        logger.warn(e) {
                            message = "Stop notification failure ignored."
                            detail(characteristic)
                        }
                    }
                }
            }
    }

    suspend fun rewire() {
        observations.entries.forEach { (characteristic, observationStartedActions) ->
            try {
                peripheral.startNotifications(characteristic)
                observationStartedActions.forEach { it() }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (t: Throwable) {
                characteristicChanges.emit(AppleObservationEvent.Error(characteristic, t))
            }
        }
    }
}

private class Observations : IsolateState<MutableMap<Characteristic, MutableList<OnSubscriptionAction>>>(
    producer = { mutableMapOf() }
) {

    val entries: Map<Characteristic, List<OnSubscriptionAction>>
        get() = access {
            mutableMapOf<Characteristic, List<OnSubscriptionAction>>().also { copy ->
                it.forEach { (key, value) ->
                    copy[key] = value.toList()
                }
            }.toMap()
        }

    fun add(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction
    ): Int = access {
        val actions = it[characteristic]
        if (actions == null) {
            val newActions = mutableListOf(onSubscription)
            newActions.ensureNeverFrozen()
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
        when {
            actions == null -> -1 // No previous observation existed for characteristic.
            actions.count() == 1 -> {
                it -= characteristic
                0
            }
            else -> {
                actions -= onSubscription
                actions.count()
            }
        }
    }
}
