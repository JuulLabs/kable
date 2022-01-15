package com.juul.kable

import co.touchlab.stately.collections.IsoMutableMap
import co.touchlab.stately.isolate.IsolateState
import com.juul.kable.logs.Logging
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import kotlin.coroutines.cancellation.CancellationException

internal expect fun Peripheral.observationHandler(): Observation.Handler

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
 *                                                        .--- acquire(A) --> A1, A2, A3
 *                             .-----------------------. /
 *  A1, B1, C1, A2, A3, B2 --> | characteristicChanges | ----- acquire(B) --> B1, B2
 *                             '-----------------------' \
 *                                                        '--- acquire(C) --> C1
 * ```
 *
 * @param peripheral to perform notification actions against to enable/disable the observations.
 */
internal class Observers<T>(
    private val peripheral: Peripheral,
    private val logging: Logging,
    extraBufferCapacity: Int = 0,
) {

    val characteristicChanges = MutableSharedFlow<ObservationEvent<T>>(extraBufferCapacity = extraBufferCapacity)
    private val observations = Observations()

    fun acquire(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<T> {
        val handler = peripheral.observationHandler()
        val observation = observations.getOrPut(characteristic) {
            Observation(peripheral.state, handler, characteristic, logging, peripheral.identifier)
        }

        return characteristicChanges
            .onSubscription { observation.onSubscription(onSubscription) }
            .filter { event -> event.isAssociatedWith(characteristic) }
            .map(::dematerialize)
            .onCompletion { observation.onCompletion(onSubscription) }
    }

    suspend fun onConnected() {
        observations.entries.forEach { (characteristic, observation) ->
            // Pipe failures to `characteristicChanges` while honoring in-flight connection cancellations.
            try {
                observation.onConnected()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (e: Exception) {
                characteristicChanges.emit(ObservationEvent.Error(characteristic, e))
            }
        }
    }
}

private class Observations : IsolateState<MutableMap<Characteristic, Observation>>(
    producer = { mutableMapOf() }
) {

    private val observations = IsoMutableMap<Characteristic, Observation>()

    val entries: Set<Map.Entry<Characteristic, Observation>>
        get() = synchronized {
            observations.entries.toSet()
        }

    fun getOrPut(
        characteristic: Characteristic,
        defaultValue: () -> Observation
    ): Observation = synchronized {
        observations.getOrPut(characteristic, defaultValue)
    }

    private val lock = SynchronizedObject()
    private inline fun <T> synchronized(block: () -> T): T = synchronized(lock, block)
}
