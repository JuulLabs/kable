package com.juul.kable

import com.juul.kable.State.Connecting.Observes
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

    private val _isObservationEnabled = atomic(false)
    private var isObservationEnabled: Boolean
        get() = _isObservationEnabled.value
        set(value) { _isObservationEnabled.value = value }

    private val isConnected: Boolean
        get() = state.value.isAtLeast<Observes>()

    private val hasSubscribers: Boolean
        get() = subscribers.isNotEmpty()

    suspend fun onSubscription(action: OnSubscriptionAction) = mutex.withLock {
        subscribers += action
        enableObservationIfNeeded()
        if (isObservationEnabled) {
            suppressConnectionExceptions {
                action()
            }
        }
    }

    suspend fun onCompletion(action: OnSubscriptionAction) = mutex.withLock {
        subscribers -= action
        disableObservationIfNeeded()
    }

    suspend fun onConnected() = mutex.withLock {
        // `force = true` is used in case a disconnected state (to reset the observation state) was missed.
        enableObservationIfNeeded(force = true)
        if (isObservationEnabled) {
            suppressConnectionExceptions {
                subscribers.forEach { it() }
            }
        }
    }

    fun onConnectionLost() {
        // We assume that remote peripheral and local BLE system implicitly clears notifications/indications.
        isObservationEnabled = false
    }

    private suspend fun enableObservationIfNeeded(force: Boolean = false) {
        if ((!isObservationEnabled || force) && isConnected && hasSubscribers) {
            suppressConnectionExceptions {
                handler.startObservation(characteristic)
                isObservationEnabled = true
            }
        }
    }

    private suspend fun disableObservationIfNeeded() {
        if (isObservationEnabled && isConnected && !hasSubscribers) {
            suppressConnectionExceptions {
                handler.stopObservation(characteristic)
            }
            isObservationEnabled = false
        }
    }

    /**
     * While spinning up or down an observation the connection may drop, resulting in an unnecessary connection related
     * exception being thrown.
     *
     * Since it is assumed that observations are automatically cleared on disconnect, these exceptions can be ignored,
     * as the corresponding [action] will be rendered unnecessary (clearing an observation is not needed if connection
     * has been lost, or [action] will be re-attempted on [reconnect][onConnected]).
     */
    private inline fun suppressConnectionExceptions(action: () -> Unit) {
        try {
            action.invoke()
        } catch (e: NotConnectedException) {
            logger.verbose { message = "Suppressed failure: $e" }
        } catch (e: BluetoothException) {
            logger.verbose { message = "Suppressed failure: $e" }
        }
    }
}
