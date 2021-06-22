package com.juul.kable

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

internal sealed class AndroidObservationEvent {

    abstract val characteristic: Characteristic

    data class CharacteristicChange(
        override val characteristic: Characteristic,
        val data: ByteArray,
    ) : AndroidObservationEvent()

    data class Error(
        override val characteristic: Characteristic,
        val cause: Throwable,
    ) : AndroidObservationEvent()
}

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

    val characteristicChanges = MutableSharedFlow<AndroidObservationEvent>()
    private val observations = Observations()

    fun acquire(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<ByteArray> = characteristicChanges
        .onSubscription {
            peripheral.suspendUntilReady()
            if (observations.add(characteristic, onSubscription) == 1) {
                peripheral.startObservation(characteristic)
            }
            onSubscription()
        }
        .filter {
            it.characteristic.characteristicUuid == characteristic.characteristicUuid &&
                it.characteristic.serviceUuid == characteristic.serviceUuid
        }
        .map {
            when (it) {
                is AndroidObservationEvent.Error -> throw it.cause
                is AndroidObservationEvent.CharacteristicChange -> it.data
            }
        }
        .onCompletion {
            if (observations.remove(characteristic, onSubscription) < 1) {
                try {
                    peripheral.stopObservation(characteristic)
                } catch (e: NotReadyException) {
                    // Silently ignore as it is assumed that failure is due to connection drop, in which case Android
                    // will clear the notifications.
                    Log.d(TAG, "Stop notification failure ignored.")
                }
            }
        }

    suspend fun rewire() {
        observations.forEach { characteristic, onSubscriptionActions ->
            try {
                peripheral.startObservation(characteristic)
                onSubscriptionActions.forEach { it() }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (t: Throwable) {
                characteristicChanges.emit(AndroidObservationEvent.Error(characteristic, t))
            }
        }
    }
}

private class Observations {

    private val lock = Mutex()
    private val observations = mutableMapOf<Characteristic, MutableList<OnSubscriptionAction>>()

    suspend inline fun forEach(
        action: (Characteristic, List<OnSubscriptionAction>) -> Unit
    ) = lock.withLock {
        observations.forEach { (characteristic, onSubscriptionActions) ->
            action(characteristic, onSubscriptionActions)
        }
    }

    suspend fun add(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Int = lock.withLock {
        val actions = observations[characteristic]
        if (actions == null) {
            val newActions = mutableListOf(onSubscription)
            observations[characteristic] = newActions
            1
        } else {
            actions += onSubscription
            actions.count()
        }
    }

    suspend fun remove(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Int = lock.withLock {
        val actions = observations[characteristic]
        when {
            actions == null -> 0
            actions.count() == 1 -> {
                observations -= characteristic
                0
            }
            else -> {
                actions -= onSubscription
                actions.count()
            }
        }
    }
}
