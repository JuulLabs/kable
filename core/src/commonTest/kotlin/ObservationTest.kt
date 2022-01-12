package com.juul.kable

import com.benasher44.uuid.uuid4
import com.juul.kable.State.Connected
import com.juul.kable.State.Disconnected
import com.juul.kable.logs.Logging
import com.juul.kable.logs.Logging.Level.Data
import com.juul.tuulbox.logging.ConsoleLogger
import com.juul.tuulbox.logging.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun generateCharacteristic() = characteristicOf(
    service = uuid4().toString(),
    characteristic = uuid4().toString(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class ObservationTest {

    private val logging = Logging().apply {
        level = Data
    }

    @BeforeTest
    fun setup() {
        Log.dispatcher.install(ConsoleLogger)
    }

    @AfterTest
    fun tearDown() {
        Log.dispatcher.clear()
    }

    @Test
    fun manySubscribers_startsObservationOnce() = runTest {
        val state = MutableStateFlow(Connected)
        val characteristic = generateCharacteristic()
        val counter = ObservationCounter(characteristic)
        val observation = Observation(state, counter, characteristic, logging, identifier = "test")

        repeat(10) {
            observation.onSubscription { }
        }
        counter.assert(
            startCount = 1,
            stopCount = 0,
        )
    }

    @Test
    fun subscribersGoesToZero_stopsObservationOnce() = runTest {
        val state = MutableStateFlow<State>(Disconnected())
        val characteristic = generateCharacteristic()
        val counter = ObservationCounter(characteristic)
        val observation = Observation(state, counter, characteristic, logging, identifier = "test")
        val onSubscriptionActions = List(10) { suspend { } }

        state.value = Connected
        onSubscriptionActions.forEach { action ->
            observation.onSubscription(action)
        }
        counter.assert(
            startCount = 1,
            stopCount = 0,
        )

        onSubscriptionActions.forEach { action ->
            observation.onCompletion(action)
        }
        counter.assert(
            startCount = 1,
            stopCount = 1,
        )
    }

    @Test
    fun subscribersGoesToZero_whileDisconnected_doesNotStopObservation() = runTest {
        val state = MutableStateFlow<State>(Connected)
        val characteristic = generateCharacteristic()
        val counter = ObservationCounter(characteristic)
        val observation = Observation(state, counter, characteristic, logging, identifier = "test")

        repeat(10) {
            observation.onSubscription { }
        }
        repeat(5) {
            observation.onCompletion { }
        }

        state.value = Disconnected()
        observation.onConnectionLost()

        repeat(5) {
            observation.onCompletion { }
        }
        counter.assert(
            startCount = 1,
            stopCount = 0,
        )
    }

    @Test
    fun hasSubscribers_reconnects_reObservesOnce() = runTest {
        val state = MutableStateFlow<State>(Connected)
        val characteristic = generateCharacteristic()
        val counter = ObservationCounter(characteristic)
        val observation = Observation(state, counter, characteristic, logging, identifier = "test")

        repeat(10) {
            observation.onSubscription { }
        }
        counter.assert(
            startCount = 1,
            stopCount = 0,
        )

        observation.onConnected() // Simulate reconnect.
        counter.assert(
            startCount = 2,
            stopCount = 0,
        )
    }

    @Test
    fun addingSubscribersDuringConnect_startsObserveOnce() = runTest {
        val state = MutableStateFlow<State>(Disconnected())
        val characteristic = generateCharacteristic()
        val counter = ObservationCounter(characteristic)
        val observation = Observation(state, counter, characteristic, logging, identifier = "test")

        repeat(5) {
            observation.onSubscription { }
        }
        counter.assert(
            startCount = 0,
            stopCount = 0,
        )

        observation.onConnected()
        counter.assert(
            startCount = 1,
            stopCount = 0,
        )

        // Simulate subscribers before state has been updated to `Connected`.
        repeat(5) {
            observation.onSubscription { }
        }
        state.value = Connected
        repeat(5) {
            observation.onSubscription { }
        }

        counter.assert(
            startCount = 1,
            stopCount = 0,
        )
    }

    @Test
    fun noSubscribers_onConnected_doesNotStartObservation() = runTest {
        val state = MutableStateFlow<State>(Disconnected())
        val characteristic = generateCharacteristic()
        val counter = ObservationCounter(characteristic)
        val observation = Observation(state, counter, characteristic, logging, identifier = "test")

        state.value = Connected
        repeat(10) {
            observation.onConnected()
        }

        counter.assert(
            startCount = 0,
            stopCount = 0,
        )
    }

    @Test
    fun onConnectedWithSubscriber_multipleTimes_startsObservationMultipleTimes() = runTest {
        val state = MutableStateFlow<State>(Disconnected())
        val characteristic = generateCharacteristic()
        val counter = ObservationCounter(characteristic)
        val observation = Observation(state, counter, characteristic, logging, identifier = "test")

        observation.onSubscription { }
        counter.assert(
            startCount = 0,
            stopCount = 0,
        )

        // Simulate numerous reconnects.
        repeat(10) {
            observation.onConnected()
        }

        counter.assert(
            startCount = 10,
            stopCount = 0,
        )
    }
}

private class ObservationCounter(
    private val characteristic: Characteristic,
) : Observation.Handler {

    var startCount = 0
    var stopCount = 0

    override suspend fun startObservation(characteristic: Characteristic) {
        if (this.characteristic == characteristic) startCount++
    }

    override suspend fun stopObservation(characteristic: Characteristic) {
        if (this.characteristic == characteristic) stopCount++
    }

    fun assert(
        startCount: Int,
        stopCount: Int,
    ) {
        assertEquals(
            expected = startCount,
            actual = this.startCount,
            message = "Start observation count",
        )
        assertEquals(
            expected = stopCount,
            actual = this.stopCount,
            message = "Stop observation count",
        )
    }
}
