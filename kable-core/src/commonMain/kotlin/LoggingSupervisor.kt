package com.juul.kable

import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * Supervisor with empty coroutine exception handler ignoring all exceptions.
 *
 * https://github.com/ktorio/ktor/blob/c9cd3308b3d0f9f1c3f5407036921e5d5aeb3f15/ktor-utils/common/src/io/ktor/util/CoroutinesUtils.kt#L23-L28
 */
@Suppress("FunctionName")
internal fun LoggingSupervisor(logging: Logging, identifier: Identifier, parent: Job? = null): CoroutineContext {
    val logger = Logger(logging, "Kable/Peripheral", identifier.toString())
    return SupervisorJob(parent).apply {
        invokeOnCompletion { cause ->
            logger.debug(cause) { message = "Supervisor job ended" }
        }
    } + CoroutineExceptionHandler { _, cause ->
        logger.warn(cause) { message = "Supervisor job received exception" }
    }
}
