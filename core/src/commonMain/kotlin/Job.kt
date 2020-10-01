package com.juul.kable

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin

internal suspend fun Job.cancelAndJoinChildren() =
    children.forEach { child -> child.cancelAndJoin() }
