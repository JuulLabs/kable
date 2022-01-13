package com.juul.kable

import com.benasher44.uuid.uuid4
import com.juul.kable.State.Connected
import com.juul.kable.State.Connecting
import com.juul.kable.State.Disconnected
import com.juul.kable.logs.LogEngine
import com.juul.kable.logs.Logging
import com.juul.kable.logs.Logging.Format.Compact
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
import kotlin.test.assertFailsWith

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
        val onSubscriptionActions = List(10) { suspend { } }

        onSubscriptionActions.forEach { action ->
            observation.onSubscription(action)
        }
        onSubscriptionActions.take(5).forEach { action ->
            observation.onCompletion(action)
        }

        state.value = Disconnected()
        observation.onConnectionLost()

        onSubscriptionActions.drop(5).forEach { action ->
            observation.onCompletion(action)
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

        // Simulate reconnect.
        state.value = Connecting.Observes
        observation.onConnected()
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

        state.value = Connecting.Observes
        observation.onConnected()
        counter.assert(
            startCount = 1,
            stopCount = 0,
        )

        // Simulate additional subscribers before state has been updated to `Connected`.
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

        state.value = Connecting.Observes
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
        state.value = Connecting.Observes
        repeat(10) {
            observation.onConnected()
        }

        counter.assert(
            startCount = 10,
            stopCount = 0,
        )
    }

    @Test
    fun connectionDropsWhileConnecting_doesNotThrow() = runTest {
        val state = MutableStateFlow<State>(Disconnected())
        val characteristic = generateCharacteristic()
        val handler = object : Observation.Handler {
            override suspend fun startObservation(characteristic: Characteristic) =
                throw NotConnectedException()

            override suspend fun stopObservation(characteristic: Characteristic) =
                throw NotConnectedException()
        }
        val logEngine = RecordingLogEngine()
        val logging = Logging().apply {
            level = Data
            format = Compact
            engine = logEngine
        }
        val identifier = "test"
        val observation = Observation(state, handler, characteristic, logging, identifier)

        observation.onSubscription { }
        state.value = Connecting.Observes
        observation.onConnected()

        assertEquals(
            expected = listOf(
                RecordingLogEngine.Record.Verbose(
                    throwable = null,
                    tag = "Kable/Observation",
                    message = "$identifier Suppressed failure: ${NotConnectedException()}",
                )
            ),
            actual = logEngine.records.toList()
        )
    }

    @Test
    fun failureDuringStartObservation_propagates() = runTest {
        val state = MutableStateFlow<State>(Disconnected())
        val characteristic = generateCharacteristic()
        val handler = object : Observation.Handler {
            override suspend fun startObservation(characteristic: Characteristic) = error("start")
            override suspend fun stopObservation(characteristic: Characteristic) {}
        }
        val observation = Observation(state, handler, characteristic, logging, identifier = "test")

        observation.onSubscription { }
        state.value = Connecting.Observes
        val failure = assertFailsWith<IllegalStateException> {
            observation.onConnected()
        }
        assertEquals(
            expected = "start",
            actual = failure.message,
        )
    }

    @Test
    fun failureDuringStopObservation_propagates() = runTest {
        val state = MutableStateFlow<State>(Connected)
        val characteristic = generateCharacteristic()
        val handler = object : Observation.Handler {
            override suspend fun startObservation(characteristic: Characteristic) {}
            override suspend fun stopObservation(characteristic: Characteristic) = error("stop")
        }
        val observation = Observation(state, handler, characteristic, logging, identifier = "test")

        val onSubscriptionAction = suspend { }
        observation.onSubscription(onSubscriptionAction)
        val failure = assertFailsWith<IllegalStateException> {
            observation.onCompletion(onSubscriptionAction)
        }
        assertEquals(
            expected = "stop",
            actual = failure.message,
        )
    }

    @Test
    fun failureInSubscriptionAction_propagates() = runTest {
        val state = MutableStateFlow<State>(Connected)
        val characteristic = generateCharacteristic()
        val counter = ObservationCounter(characteristic)
        val observation = Observation(state, counter, characteristic, logging, identifier = "test")

        val onSubscriptionAction = suspend { error("action") }
        val failure = assertFailsWith<IllegalStateException> {
            observation.onSubscription(onSubscriptionAction)
        }
        assertEquals(
            expected = "action",
            actual = failure.message,
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

private class RecordingLogEngine : LogEngine {

    val records = mutableListOf<Record>()

    sealed class Record {
        abstract val throwable: Throwable?
        abstract val tag: String
        abstract val message: String

        data class Verbose(
            override val throwable: Throwable?,
            override val tag: String,
            override val message: String,
        ) : Record()

        data class Debug(
            override val throwable: Throwable?,
            override val tag: String,
            override val message: String,
        ) : Record()

        data class Info(
            override val throwable: Throwable?,
            override val tag: String,
            override val message: String,
        ) : Record()

        data class Warn(
            override val throwable: Throwable?,
            override val tag: String,
            override val message: String,
        ) : Record()

        data class Error(
            override val throwable: Throwable?,
            override val tag: String,
            override val message: String,
        ) : Record()

        data class Assert(
            override val throwable: Throwable?,
            override val tag: String,
            override val message: String,
        ) : Record()
    }

    override fun verbose(throwable: Throwable?, tag: String, message: String) {
        records += Record.Verbose(throwable, tag, message)
    }

    override fun debug(throwable: Throwable?, tag: String, message: String) {
        records += Record.Debug(throwable, tag, message)
    }

    override fun info(throwable: Throwable?, tag: String, message: String) {
        records += Record.Info(throwable, tag, message)
    }

    override fun warn(throwable: Throwable?, tag: String, message: String) {
        records += Record.Warn(throwable, tag, message)
    }

    override fun error(throwable: Throwable?, tag: String, message: String) {
        records += Record.Error(throwable, tag, message)
    }

    override fun assert(throwable: Throwable?, tag: String, message: String) {
        records += Record.Assert(throwable, tag, message)
    }
}
