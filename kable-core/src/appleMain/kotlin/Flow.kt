package com.juul.kable

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking

internal fun <T> MutableSharedFlow<T>.emitBlocking(value: T) {
    if (tryEmit(value)) return
    runBlocking { emit(value) }
}
