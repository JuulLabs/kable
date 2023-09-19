package com.juul.kable

import kotlinx.coroutines.CancellationException

internal fun Throwable.unwrap(): Throwable {
    var exception: Throwable = this
    while (exception is CancellationException) {
        if (exception == exception.cause) return this
        exception = exception.cause ?: return this
        if (exception is IOException || exception is BluetoothException) return exception
    }
    return this
}
