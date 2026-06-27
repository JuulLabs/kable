package com.juul.kable

import com.juul.kable.ObservationEvent.CharacteristicChange
import com.juul.kable.ObservationEvent.Error
import com.juul.kable.logs.Logging
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

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
    private val forceCharacteristicEqualityByUuid: Boolean,
    private val exceptionHandler: ObservationExceptionHandler,
) {

    val characteristicChanges = MutableSharedFlow<ObservationEvent<T>>(extraBufferCapacity = Int.MAX_VALUE)

    private val observations = mutableMapOf<Characteristic, Observation>()
    private val lock = SynchronizedObject()

    fun acquire(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<T> {
        val state = peripheral.state
        val handler = peripheral.observationHandler()
        val identifier = peripheral.identifier

        val observation = synchronized(lock) {
            observations.getOrPut(characteristic) {
                Observation(state, handler, characteristic, logging, identifier.toString())
            }
        }

        return characteristicChanges
            .onSubscription {
                try {
                    observation.onSubscription(onSubscription)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    exceptionHandler(ObservationExceptionPeripheral(peripheral), e)
                }
            }
            .filter { event -> event.isAssociatedWith(characteristic, forceCharacteristicEqualityByUuid) }
            .onEach { event ->
                if (event is Error) {
                    exceptionHandler(ObservationExceptionPeripheral(peripheral), event.cause)
                }
            }
            .mapNotNull { event -> (event as? CharacteristicChange)?.data }
            .onCompletion {
                try {
                    // `NonCancellable` used to prevent interruption of resetting the observation
                    // state, which can prevent subsequent re-observation.
                    // See https://github.com/JuulLabs/kable/issues/677 for more details.
                    withContext(NonCancellable) {
                        observation.onCompletion(onSubscription)
                    }
                } catch (e: Exception) {
                    coroutineContext.ensureActive()
                    exceptionHandler(ObservationExceptionPeripheral(peripheral), e)
                }
            }
    }

    suspend fun onConnected() {
        synchronized(lock) {
            observations.entries.toSet()
        }.forEach { (_, observation) ->
            // Pipe failures to `characteristicChanges` while honoring in-flight connection cancellations.
            try {
                observation.onConnected()
            } catch (e: Exception) {
                coroutineContext.ensureActive()
                throw IOException("Failed to observe characteristic during connection attempt", e)
            }
        }
    }
}
