package com.juul.kable

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import org.khronos.webgl.DataView

internal data class CharacteristicChange(
    val characteristic: Characteristic,
    val data: DataView,
)

internal class Observers(
    private val peripheral: JsPeripheral
) {

    val characteristicChanges = MutableSharedFlow<CharacteristicChange>(extraBufferCapacity = 64)
    private val observations = Observations()

    fun acquire(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction,
    ): Flow<DataView> = characteristicChanges
        .onSubscription {
            peripheral.suspendUntilReady()
            if (observations.add(characteristic, onSubscription) == 1) {
                peripheral.startObservation(characteristic)
            }
            onSubscription()
        }
        .filter {
            it.characteristic.characteristicUuid == characteristic.characteristicUuid
        }
        .map { it.data }
        .onCompletion {
            if (observations.remove(characteristic, onSubscription) < 1) {
                peripheral.stopObservation(characteristic)
            }
        }

    suspend fun rewire() {
        observations.entries.forEach { (characteristic, onSubscriptionActions) ->
            peripheral.startObservation(characteristic)
            onSubscriptionActions.forEach { it() }
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
