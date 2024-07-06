package com.juul.kable

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SharedRepeatableActionTests {

    @Test
    fun exceptionThrownFromAction_cancelsCoroutinesLaunchedFromScope() = runTest {
        supervisorScope {
            lateinit var innerJob: Job
            val started = MutableStateFlow(false)
            val asserted = MutableStateFlow(false)

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
                ++actionRuns
            }

            assertEquals(
                expected = 1,
                actual = action.await(),
            )

            actionDidComplete.value = true
            action.join()

            assertEquals(
                expected = 2,
                actual = action.await(),
            )
        }
    }

    @Test
    fun exceptionThrownFromCoroutineLaunchedFromScope_cancelsAction() = runTest {
        supervisorScope {
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
    }

    @Test
    fun actionIsCancelled_canStartAgain() = runTest {
        val innerRuns = Channel<Int>()
        var innerCounter = 0
        val actionRuns = Channel<Int>()
        var actionCounter = 0

        val testScope = CoroutineScope(
            coroutineContext +
                SupervisorJob(coroutineContext.job) +
                CoroutineExceptionHandler { _, cause -> cause.printStackTrace() },
        )
        val action = testScope.sharedRepeatableAction { scope ->
            scope.launch {
                innerRuns.send(++innerCounter)
                awaitCancellation()
            }
            actionRuns.send(++actionCounter)
            awaitCancellation()
        }
        val job = testScope.launch(start = UNDISPATCHED) { action.await() }

        assertEquals(
            expected = 1,
            actual = innerRuns.receive(),
        )
        assertEquals(
            expected = 1,
            actual = actionRuns.receive(),
        )

        action.cancelAndJoin()
        assertTrue { job.isCancelled }

        testScope.launch(start = UNDISPATCHED) { action.await() }
        assertEquals(
            expected = 2,
            actual = innerRuns.receive(),
        )
        assertEquals(
            expected = 2,
            actual = actionRuns.receive(),
        )

        job.join()
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

    @Test
    fun honorsCancellationOfParentScope() = runTest {
        val didCancel = MutableStateFlow(false)
        val parentScope = CoroutineScope(
            coroutineContext +
                SupervisorJob(coroutineContext.job) +
                CoroutineExceptionHandler { _, cause -> cause.printStackTrace() },
        )
        val action = parentScope.sharedRepeatableAction { scope ->
            scope.launch(start = UNDISPATCHED) {
                try {
                    awaitCancellation()
                } catch (e: Exception) {
                    if (e is CancellationException) didCancel.value = true
                    throw e
                }
            }
            1
        }

        assertEquals(
            expected = 1,
            actual = action.await(),
        )

        parentScope.cancel()
        didCancel.first { it }
    }
}
