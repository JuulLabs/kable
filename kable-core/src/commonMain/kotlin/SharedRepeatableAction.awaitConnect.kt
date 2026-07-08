package com.juul.kable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.cancellation.CancellationException

internal suspend fun SharedRepeatableAction<CoroutineScope>.awaitConnect(): CoroutineScope =
    try {
        await()
    } catch (e: CancellationException) {
        // A disconnect cancels the shared action, while caller cancellation must remain a
        // CancellationException. `ensureActive()` distinguishes between the two cases.
        // https://github.com/JuulLabs/kable/issues/1136
        currentCoroutineContext().ensureActive()

        throw IllegalStateException("Cannot connect peripheral that has been cancelled", e)
    } catch (e: InactiveScopeException) {
        throw IllegalStateException("Cannot connect peripheral that has been cancelled", e)
    }
