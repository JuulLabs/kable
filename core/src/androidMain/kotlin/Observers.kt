package com.juul.kable

import com.juul.kable.logs.Logging
import com.juul.tuulbox.collections.synchronizedMapOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
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
    private val state: StateFlow<State>,
    private val logging: Logging,
) {

    val characteristicChanges = MutableSharedFlow<AndroidObservationEvent>()
    private val observations = synchronizedMapOf<Characteristic, Observation>()

    fun acquire(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<ByteArray> {
        val handler = peripheral.observationHandler()
        val identifier = peripheral.bluetoothDevice.address
        val observation = observations.synchronized {
            getOrPut(characteristic) {
                Observation(state, handler, characteristic, logging, identifier)
            }
        }

        return characteristicChanges
            .onSubscription { observation.onSubscription(onSubscription) }
            .filter {
                it.characteristic.characteristicUuid == characteristic.characteristicUuid &&
                    it.characteristic.serviceUuid == characteristic.serviceUuid
            }
            .map(::dematerialize)
            .onCompletion { observation.onCompletion(onSubscription) }
    }

    suspend fun onConnected() {
        observations.entries.forEach { (characteristic, observation) ->
            try {
                observation.onConnected()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (e: Exception) {
                characteristicChanges.emit(AndroidObservationEvent.Error(characteristic, e))
            }
        }
    }

    fun onConnectionLost() {
        observations.entries.forEach { (_, observation) ->
            observation.onConnectionLost()
        }
    }
}

private fun dematerialize(event: AndroidObservationEvent): ByteArray = when (event) {
    is AndroidObservationEvent.Error -> throw event.cause
    is AndroidObservationEvent.CharacteristicChange -> event.data
}

private fun AndroidPeripheral.observationHandler(): Observation.Handler = object : Observation.Handler {
    override suspend fun startObservation(characteristic: Characteristic) {
        this@observationHandler.startObservation(characteristic)
    }

    override suspend fun stopObservation(characteristic: Characteristic) {
        this@observationHandler.stopObservation(characteristic)
    }
}
