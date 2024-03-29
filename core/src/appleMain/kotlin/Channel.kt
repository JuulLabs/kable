package com.juul.kable

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking

internal fun <E> SendChannel<E>.sendBlocking(element: E) {
    if (trySend(element).isSuccess) return
    runBlocking { send(element) }
}
