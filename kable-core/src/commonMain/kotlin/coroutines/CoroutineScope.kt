package com.juul.kable.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job

internal fun CoroutineScope.childSupervisor(name: String) =
    CoroutineScope(coroutineContext + SupervisorJob(coroutineContext.job) + CoroutineName(name))
