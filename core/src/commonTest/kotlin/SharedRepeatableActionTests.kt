package com.juul.kable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SharedRepeatableActionTests {

    @Test
    fun exceptionThrownFromAction_cancelsCoroutinesLaunchedFromScope() = runTest {
        lateinit var innerJob: Job
        val started = MutableStateFlow(false)
        val asserted = MutableStateFlow(false)

        val action = sharedRepeatableAction { scope ->
            innerJob = scope.launch(start = UNDISPATCHED) {
                awaitCancellation()
            }
            started.value = true
            asserted.first { it }
            throw IllegalStateException("ouch")
        }

        // GlobalScope used to prevent propagation of failure to the parent runTest scope.
        val deferred = GlobalScope.async { action.await() }

        started.first { it }
        assertFalse { innerJob.isCompleted }
        asserted.value = true

        assertFailsWith<IllegalStateException> {
            deferred.await()
        }
        assertTrue { innerJob.isCompleted }
    }

    @Test
    fun exceptionThrownFromCoroutineLaunchedFromScope_cancelsAction() = runTest {
        val action = sharedRepeatableAction { scope ->
            scope.launch {
                throw IllegalStateException()
            }
            awaitCancellation()
        }

        assertFailsWith<CancellationException> {
            action.await()
        }
    }

    @Test
    fun actionIsCancelled_canStartAgain() = runTest {
        val innerRuns = Channel<Int>()
        var innerCounter = 0
        val actionRuns = Channel<Int>()
        var actionCounter = 0

        val action = sharedRepeatableAction { scope ->
            scope.launch {
                innerRuns.send(++innerCounter)
                awaitCancellation()
            }
            actionRuns.send(++actionCounter)
            awaitCancellation()
        }
        val job = launch(start = UNDISPATCHED) { action.await() }

        assertEquals(
            expected = 1,
            actual = innerRuns.receive(),
        )
        assertEquals(
            expected = 1,
            actual = actionRuns.receive(),
        )

        action.resetAndJoin()
        assertTrue { job.isCancelled }

        launch(start = UNDISPATCHED) { action.await() }
        assertEquals(
            expected = 2,
            actual = innerRuns.receive(),
        )
        assertEquals(
            expected = 2,
            actual = actionRuns.receive(),
        )

        coroutineContext.cancelChildren()
    }

    @Test
    fun multipleCallers_allGetResultOfAction() = runTest {
        val action = sharedRepeatableAction { scope ->
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

        coroutineContext.cancelChildren()
    }

    @Test
    fun nothingLaunchedFromScope_remainsActive() = runTest {
        lateinit var actionScope: CoroutineScope
        val action = sharedRepeatableAction { scope ->
            actionScope = scope
            1
        }

        assertEquals(
            expected = 1,
            actual = action.await(),
        )
        assertTrue { actionScope.isActive }

        coroutineContext.cancelChildren()
    }
}
