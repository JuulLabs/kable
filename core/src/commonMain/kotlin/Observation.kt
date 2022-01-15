package com.juul.kable

import co.touchlab.stately.collections.IsoMutableList
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
    private val subscribers = IsoMutableList<OnSubscriptionAction>()

    private val _didStartObservation = atomic(false)
    private var didStartObservation: Boolean
        get() = _didStartObservation.value
        set(value) { _didStartObservation.value = value }

    private val isConnected: Boolean
        get() = state.value.isAtLeast<Observes>()

    private val hasSubscribers: Boolean
        get() = subscribers.isNotEmpty()

    suspend fun onSubscription(action: OnSubscriptionAction) = mutex.withLock {
        subscribers += action
        val shouldStartObservation = !didStartObservation && hasSubscribers && isConnected
        if (shouldStartObservation) {
            suppressConnectionExceptions {
                startObservation()
                action()
            }
        }
    }

    suspend fun onCompletion(action: OnSubscriptionAction) = mutex.withLock {
        subscribers -= action
        val shouldStopObservation = didStartObservation && !hasSubscribers && isConnected
        if (shouldStopObservation) stopObservation()
    }

    suspend fun onConnected() = mutex.withLock {
        if (hasSubscribers && isConnected) {
            suppressConnectionExceptions {
                startObservation()
                subscribers.forEach { it() }
            }
        }
    }

    private suspend fun startObservation() {
        handler.startObservation(characteristic)
        didStartObservation = true
    }

    private suspend fun stopObservation() {
        suppressConnectionExceptions {
            handler.stopObservation(characteristic)
        }
        didStartObservation = false
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
