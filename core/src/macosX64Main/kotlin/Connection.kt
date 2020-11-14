package com.juul.kable

import com.juul.kable.PeripheralDelegate.DidUpdateValueForCharacteristic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class Connection(
    val delegate: PeripheralDelegate,
) {

    val characteristicChanges: Flow<DidUpdateValueForCharacteristic.Data> =
        delegate.characteristicChanges.transform {
            if (it is DidUpdateValueForCharacteristic.Data) emit(it)
        }

    val mutex = Mutex()

    suspend inline fun <T> execute(
        action: () -> Unit,
    ): T {
        println("⎵ Connection.execute")
        val result = mutex.withLock {
            println("Lock ENTER")
            action.invoke()
            val response = delegate.response.receive()
            val error = response.error
            if (error != null) throw IOException(error.description, cause = null)
            println("Lock EXIT")
            response as T
        }
        println("⎴ Connection.execute")
        return result
    }
}
