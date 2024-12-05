package com.juul.kable

import kotlinx.coroutines.CoroutineScope

internal suspend fun SharedRepeatableAction<CoroutineScope>.awaitConnect(): CoroutineScope =
    try {
        await()
    } catch (e: IllegalStateException) {
        throw IllegalStateException("Cannot connect peripheral that has been cancelled", e)
    }
