package com.juul.kable

import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.coroutines.CoroutineContext

internal class Connection(
    parentCoroutineContext: CoroutineContext,
    val delegate: PeripheralDelegate,
    logging: Logging,
    identifier: String,
) {

    private val scope = CoroutineScope(parentCoroutineContext + Job(parentCoroutineContext[Job]) + CoroutineName("Kable/Connection@$identifier"))

    private val logger = Logger(logging, tag = "Kable/Connection", identifier = identifier)

    // Using Semaphore as Mutex never relinquished lock when multiple concurrent `withLock`s are executed.
    val semaphore = Semaphore(1)

    private var deferredResponse: Deferred<PeripheralDelegate.Response>? = null

    suspend inline fun <T> execute(
        action: () -> Unit,
    ): T = semaphore.withPermit {
        deferredResponse?.let {
            if (it.isActive) {
                // Discard response as we've performed another `execute` without the previous finishing. This happens if
                // a previous `execute` was cancelled after invoking GATT action, but before receiving response from
                // callback channel. See the following issues for more details:
                // https://github.com/JuulLabs/kable/issues/326
                // https://github.com/JuulLabs/kable/issues/450
                val response = it.await()
                logger.warn {
                    message = "Discarded response"
                    detail("response", response.toString())
                }
            }
        }

        action.invoke()
        val deferred = scope.async { delegate.response.receive() }
        deferredResponse = deferred

        val response = try {
            deferred.await()
        } catch (e: ConnectionLostException) {
            throw ConnectionLostException(cause = e)
        }
        deferredResponse = null

        val error = response.error
        if (error != null) throw IOException(error.description, cause = null)
        response as T
    }

    fun close() {
        scope.cancel()
        delegate.close()
    }
}
