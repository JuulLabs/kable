package com.juul.kable

import kotlinx.coroutines.channels.SendChannel

// Workaround until https://github.com/Kotlin/kotlinx.coroutines/issues/974 is closed.
internal fun <E> SendChannel<E>.offerCatching(element: E): Boolean {
    return runCatching { offer(element) }.getOrDefault(false)
}
