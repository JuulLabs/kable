package com.juul.kable

import co.touchlab.stately.collections.IsoMutableList
import co.touchlab.stately.isolate.IsolateState
import com.juul.kable.logs.Logging
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
        val state = peripheral.state
        val handler = peripheral.observationHandler()
        val identifier = peripheral.identifier

        // `IsoMutableList` created outside of `getOrPut`, because it would deadlock on Native if created in
        // `Observation` constructor.
        val subscribers = IsoMutableList<OnSubscriptionAction>()

        val observation = observations.getOrPut(characteristic) {
            Observation(state, handler, characteristic, logging, identifier, subscribers)
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

    val entries: List<Pair<Characteristic, Observation>>
        get() = access { observations ->
            // `map` used as a means to copy entries, to prevent freeze exceptions on Native.
            observations.entries.map { (characteristic, observation) ->
                characteristic to observation
            }
        }

    fun getOrPut(
        characteristic: Characteristic,
        defaultValue: () -> Observation
    ): Observation = access { observations ->
        observations.getOrPut(characteristic, defaultValue)
    }
}
