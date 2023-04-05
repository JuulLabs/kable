package com.juul.kable

import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

internal class Connection(
    val delegate: PeripheralDelegate,
    logging: Logging,
    identifier: String,
) {

    private val logger = Logger(logging, tag = "Kable/Connection", identifier = identifier)

    // Using Semaphore as Mutex never relinquished lock when multiple concurrent `withLock`s are executed.
    val semaphore = Semaphore(1)

    private var pending = false

    suspend inline fun <T> execute(
        action: () -> Unit,
    ): T = semaphore.withPermit {
        if (pending) {
            // Discard response as we've performed another `execute` without the previous finishing. This happens if a
            // previous `execute` was cancelled after invoking GATT action, but before receiving response from callback
            // channel. See https://github.com/JuulLabs/kable/issues/326 for more details.
            val response = try {
                delegate.response.receive()
            } finally {
                pending = false
            }
            logger.warn {
                message = "Discarded response"
                detail("response", response.toString())
            }
        }

        pending = true
        action.invoke()
        val response = delegate.response.receive()
        val error = response.error
        if (error == null) {
            pending = false
        } else {
            throw IOException(error.description, cause = null)
        }
        response as T
    }

    fun close() {
        delegate.close()
    }
}
