package com.juul.kable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SharedRepeatableActionTests {

    @Test
    fun exceptionThrownFromAction_cancelsCoroutinesLaunchedFromScope() = runTest {
        lateinit var innerJob: Job
        val started = MutableStateFlow(false)
        val asserted = MutableStateFlow(false)

        supervisorScope {
            val action = sharedRepeatableAction { scope ->
                innerJob = scope.launch(start = UNDISPATCHED) {
                    awaitCancellation()
                }
                started.value = true
                asserted.first { it }
                throw IllegalStateException()
            }

            val deferred = async { action.await() }
            started.first { it }
            assertFalse { innerJob.isCompleted }
            asserted.value = true

            assertFailsWith<IllegalStateException> {
                deferred.await()
            }
            assertTrue { innerJob.isCompleted }
        }
    }

    @Test
    fun actionCompletes_failureFromLaunch_canStartAgain() = runTest {
        val actionDidComplete = MutableStateFlow(false)
        var actionRuns = 0

        supervisorScope {
            val action = sharedRepeatableAction { scope ->
                scope.launch {
                    actionDidComplete.first { it }
                    throw IllegalStateException()
                }
                actionRuns++
                scope
            }

            val innerScope = action.await()
            assertEquals(
                expected = 1,
                actual = actionRuns,
            )

            actionDidComplete.value = true
            innerScope.coroutineContext.job.join()

            action.await()
            assertEquals(
                expected = 2,
                actual = actionRuns,
            )
        }
    }

    @Test
    fun exceptionThrownFromCoroutineLaunchedFromAction_cancelsAction() = runTest {
        supervisorScope {
            val action = sharedRepeatableAction { scope ->
                scope.launch {
                    throw IllegalStateException()
                }
                awaitCancellation()
            }

            val cancellation = assertFailsWith<CancellationException> {
                action.await()
            }

            assertIs<IllegalStateException>(cancellation.cause)
        }
    }

    @Test
    fun actionAwaitIsCancelled_actionStillActive_awaitsSameDeferred() = runTest {
        val testScope = CoroutineScope(Job())

        val actions = MutableStateFlow(0)
        val ready = Channel<Unit>()

        val action = testScope.sharedRepeatableAction {
            actions.update { it + 1 }
            ready.receive()
        }

        val deferred1 = async(start = UNDISPATCHED) {
            action.await()
        }
        deferred1.cancel()
        assertEquals(
            expected = 1,
            actual = actions.filterNot { it == 0 }.first(),
            message = "First action await",
        )

        val deferred2 = async(start = UNDISPATCHED) {
            launch {
                ready.send(Unit)
            }
            action.await()
        }
        deferred2.await()

        // Validate that we `await`ed the same active "action".
        assertEquals(
            expected = 1,
            actual = actions.value,
            message = "Resumed action await",
        )

        testScope.cancel()
    }

    @Test
    fun multipleCallers_allGetResultOfAction() = runTest {
        val testScope = CoroutineScope(SupervisorJob())

        val action = testScope.sharedRepeatableAction { scope ->
            scope.launch(start = UNDISPATCHED) {
                awaitCancellation()
            }
            1
        }

        val results = (1..10).map {
            async { action.await() }
        }.awaitAll()

        assertEquals(
            expected = List(10) { 1 },
            actual = results,
        )

        testScope.cancel()
    }

    @Test
    fun nothingLaunchedFromScope_remainsActive() = runTest {
        val testScope = CoroutineScope(SupervisorJob())

        lateinit var actionScope: CoroutineScope
        val action = testScope.sharedRepeatableAction { scope ->
            actionScope = scope
            1
        }

        assertEquals(
            expected = 1,
            actual = action.await(),
        )
        yield()
        assertTrue { actionScope.isActive }

        testScope.cancel()
    }

    @Test
    fun honorsCancellationOfParentScope() = runTest {
        val parentScope = CoroutineScope(SupervisorJob())

        val cancellation = MutableStateFlow<Throwable?>(null)
        val action = parentScope.sharedRepeatableAction { scope ->
            scope.launch(start = UNDISPATCHED) {
                try {
                    awaitCancellation()
                } catch (e: Exception) {
                    if (e is CancellationException) cancellation.value = e
                    throw e
                }
            }
            1
        }

        assertEquals(
            expected = 1,
            actual = action.await(),
        )

        parentScope.cancel(CancellationException("testing"))
        val e = cancellation.filterNotNull().first()
        assertIs<CancellationException>(e)
        assertEquals(
            expected = "testing",
            actual = e.message,
        )
    }

    @Test
    fun awaitWithLaunch_awaitTwice_completes() = runTest {
        val testScope = CoroutineScope(SupervisorJob())

        var launches = 0
        var actions = 0
        val didLaunch = Channel<Unit>()

        val action = testScope.sharedRepeatableAction { scope ->
            scope.launch {
                launches++
                didLaunch.send(Unit)
                awaitCancellation()
            }
            actions++
            scope
        }

        action.await() // A "connect" would invoke this.
        didLaunch.receive()
        action.cancelAndJoin(null) // A "disconnect" would first invoke this..

        action.await() // A "re-connect" would invoke this.
        didLaunch.receive()
        action.cancelAndJoin(null) // A "disconnect" the 2nd time would invoke this...

        assertEquals(launches, 2, "Launch within action")
        assertEquals(actions, 2, "Action lambda execution")

        testScope.cancel()
    }

    @Test
    fun simulation_cancelConnect() = runTest {
        val testScope = CoroutineScope(SupervisorJob())

        val action = testScope.sharedRepeatableAction {
            awaitCancellation() // Simulate long connection process.
        }

        lateinit var caught: Throwable
        val job = launch(start = UNDISPATCHED) {
            // Simulate triggering a connection process.
            try {
                action.await()
            } catch (e: Exception) {
                caught = e
            }
        }

        action.cancelAndJoin(
            CancellationException("Simulated disconnect", IllegalStateException("disconnect")),
        )
        job.join()

        val e = assertIs<CancellationException>(caught)
        assertEquals(
            expected = "Simulated disconnect",
            actual = e.message,
        )
        assertIs<IllegalStateException>(e.cause)
    }
}
