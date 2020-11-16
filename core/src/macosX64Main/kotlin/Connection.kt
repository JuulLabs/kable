package com.juul.kable

import com.juul.kable.PeripheralDelegate.DidUpdateValueForCharacteristic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

internal class Connection(
    private val delegate: PeripheralDelegate,
) {

    val characteristicChanges: Flow<DidUpdateValueForCharacteristic> =
        delegate.characteristicChanges

    // Using Semaphore as Mutex never relinquished lock when multiple concurrent `withLock`s are executed.
    val semaphore = Semaphore(1)

    suspend inline fun <T> execute(
        action: () -> Unit,
    ): T = semaphore.withPermit {
        action.invoke()
        val response = delegate.response.receive()
        val error = response.error
        if (error != null) throw IOException(error.description, cause = null)
        response as T
    }

    fun close() {
        delegate.close()
    }
}
