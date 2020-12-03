package com.juul.kable

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin

internal val Job.isNotCancelled: Boolean
    get() = !isCancelled

internal suspend fun Job.cancelAndJoinChildren() =
    children.forEach { child -> child.cancelAndJoin() }
