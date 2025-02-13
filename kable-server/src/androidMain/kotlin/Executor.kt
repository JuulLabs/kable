package com.juul.kable.server

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class Executor {

    private val guard = Mutex()

    suspend fun <T> execute(action: suspend () -> T): T = guard.withLock {
        TODO()
    }

    fun addService() {

    }
}
