package com.juul.kable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SharedRepeatableActionAwaitConnectTests {

    @Test
    fun illegalStateExceptionFromAction_isPreserved() = runTest {
        val expected = IllegalStateException("Missing BLUETOOTH_CONNECT permission")
        val actionScope = CoroutineScope(SupervisorJob())
        val action: SharedRepeatableAction<CoroutineScope> = actionScope.sharedRepeatableAction {
            throw expected
        }

        val actual = assertFailsWith<IllegalStateException> {
            action.awaitConnect()
        }

        assertEquals(expected.message, actual.message)
        actionScope.cancel()
    }

    @Test
    fun inactiveParentScope_isReportedAsCancelledPeripheral() = runTest {
        val parentScope = CoroutineScope(SupervisorJob())
        val action = parentScope.sharedRepeatableAction { it }
        parentScope.cancel()

        val exception = assertFailsWith<IllegalStateException> {
            action.awaitConnect()
        }

        assertEquals("Cannot connect peripheral that has been cancelled", exception.message)
    }

    @Test
    fun cancelledAction_isReportedAsCancelledPeripheral() = runTest {
        val actionScope = CoroutineScope(coroutineContext + SupervisorJob())
        val action: SharedRepeatableAction<CoroutineScope> = actionScope.sharedRepeatableAction {
            awaitCancellation()
        }

        supervisorScope {
            val deferred = async(start = UNDISPATCHED) {
                action.awaitConnect()
            }
            action.cancelAndJoin(CancellationException("Disconnect requested"))

            val exception = assertFailsWith<IllegalStateException> {
                deferred.await()
            }

            assertEquals("Cannot connect peripheral that has been cancelled", exception.message)
        }
        actionScope.cancel()
    }
}
