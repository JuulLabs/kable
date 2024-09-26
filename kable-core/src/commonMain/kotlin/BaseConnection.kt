package com.juul.kable

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext

internal abstract class BaseConnection(
    parentContext: CoroutineContext,
    name: String,
) : CoroutineScope {

    val job = Job(parentContext.job)
    override val coroutineContext = parentContext + job + CoroutineName(name)
}
