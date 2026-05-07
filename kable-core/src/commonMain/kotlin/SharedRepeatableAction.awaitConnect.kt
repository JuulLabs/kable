package com.juul.kable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal suspend fun SharedRepeatableAction<CoroutineScope>.awaitConnect(): CoroutineScope =
    try {
        await()
    } catch (e: IllegalStateException) {
        // If calling code cancels we'd unintentionally swallow the `CancellationException`, as it
        // is a subclass of `IllegalStateException`. `ensureActive()` honors caller cancellation.
        // https://github.com/JuulLabs/kable/issues/1136
        currentCoroutineContext().ensureActive()

        throw IllegalStateException("Cannot connect peripheral that has been cancelled", e)
    }
