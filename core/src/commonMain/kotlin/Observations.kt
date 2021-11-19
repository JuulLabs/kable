package com.juul.kable

import com.juul.kable.State.Connected
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class Observation(
    private val state: StateFlow<State>,
    private val handler: Handler,
    private val characteristic: Characteristic,
    logging: Logging,
    identifier: String,
) {

    interface Handler {
        suspend fun startObservation(characteristic: Characteristic)
        suspend fun stopObservation(characteristic: Characteristic)
    }

    private val logger = Logger(logging, tag = "Kable/Observation", identifier)

    private val mutex = Mutex()
    private val subscribers = mutableListOf<OnSubscriptionAction>()
    private val isObservationEnabled = atomic(false)

    private val isConnected: Boolean
        get() = state.value is Connected

    private val hasSubscribers: Boolean
        get() = subscribers.isNotEmpty()

    suspend fun onSubscription(action: OnSubscriptionAction) = mutex.withLock {
        subscribers += action
        enableObservationIfNeeded()
        if (isObservationEnabled.value) {
            // Ignore `NotConnectedException` to guard against potential race-condition where disconnect occurs
            // immediately after checking `isObservationEnabled`.
            suppressNotConnectedException {
                action()
            }
        }
    }

    suspend fun onCompletion(action: OnSubscriptionAction) = mutex.withLock {
        subscribers -= action
        disableObservationIfNeeded()
    }

    suspend fun onConnected() = mutex.withLock {
        enableObservationIfNeeded()
        if (isObservationEnabled.value) {
            // Ignore `NotConnectedException` to guard against potential race-condition where disconnect occurs
            // immediately after checking `isObservationEnabled`.
            suppressNotConnectedException {
                subscribers.forEach { it() }
            }
        }
    }

    fun onConnectionLost() {
        // We assume that remote peripheral and local BLE system implicitly clears notifications/indications.
        isObservationEnabled.value = false
    }

    private suspend fun enableObservationIfNeeded() {
        if (!isObservationEnabled.value && isConnected && hasSubscribers) {
            suppressNotConnectedException {
                handler.startObservation(characteristic)
                isObservationEnabled.value = true
            }
        }
    }

    private suspend fun disableObservationIfNeeded() {
        if (isObservationEnabled.value && isConnected && !hasSubscribers) {
            suppressNotConnectedException {
                handler.stopObservation(characteristic)
            }
            isObservationEnabled.value = false
        }
    }

    /**
     * It is assumed that observations are automatically cleared on disconnect, therefore in some situations
     * [NotConnectedException]s can be ignored, as the corresponding [action] will be rendered unnecessary
     * (e.g. clearing an observation is not needed if connection has been lost), or [action] will be re-attempted on
     * [reconnect][onConnected].
     */
    private inline fun suppressNotConnectedException(action: () -> Unit) {
        try {
            action.invoke()
        } catch (e: NotConnectedException) {
            logger.verbose { message = "Suppressed failure: ${e.message}" }
        }
    }
}
