package com.juul.kable

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import org.khronos.webgl.DataView
import kotlin.coroutines.cancellation.CancellationException

internal sealed class JsObservationEvent {

    abstract val characteristic: Characteristic

    data class CharacteristicChange(
        override val characteristic: Characteristic,
        val data: DataView,
    ) : JsObservationEvent()

    data class Error(
        override val characteristic: Characteristic,
        val cause: Throwable,
    ) : JsObservationEvent()
}

internal class Observers(
    private val peripheral: JsPeripheral,
) {

    val characteristicChanges = MutableSharedFlow<JsObservationEvent>(extraBufferCapacity = 64)
    private val observations = Observations()

    fun acquire(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<DataView> = characteristicChanges
        .onSubscription {
            peripheral.suspendUntil<State.Connecting.Observes>()
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
                is JsObservationEvent.Error -> throw it.cause
                is JsObservationEvent.CharacteristicChange -> it.data
            }
        }
        .onCompletion {
            if (observations.remove(characteristic, onSubscription) == 0) {
                peripheral.stopObservation(characteristic)
            }
        }

    suspend fun rewire() {
        observations.entries.forEach { (characteristic, onSubscriptionActions) ->
            try {
                peripheral.startObservation(characteristic)
                onSubscriptionActions.forEach { it() }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (t: Throwable) {
                characteristicChanges.emit(JsObservationEvent.Error(characteristic, t))
            }
        }
    }
}

private class Observations {

    private val observations = mutableMapOf<Characteristic, MutableList<OnSubscriptionAction>>()
    val entries get() = observations.entries

    fun add(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Int {
        val actions = observations[characteristic]
        return if (actions == null) {
            val newActions = mutableListOf(onSubscription)
            observations[characteristic] = newActions
            1
        } else {
            actions += onSubscription
            actions.count()
        }
    }

    fun remove(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Int {
        val actions = observations[characteristic]
        return when {
            actions == null -> -1 // No previous observation existed for characteristic.
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
