package com.juul.kable.server

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

internal fun CoroutineScope.createChildScope(): CoroutineScope = TODO()

internal suspend fun CoroutineContext.cancelAndJoinChildren() {
    job.children.apply {
        forEach(Job::cancel)
        forEach { it.join() }
    }
}
