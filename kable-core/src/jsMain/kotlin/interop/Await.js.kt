package com.juul.kable.interop

import kotlin.js.Promise
import kotlinx.coroutines.await as kotlinxAwait

internal actual suspend fun <T : JsAny?> Promise<T>.await(): T = kotlinxAwait()
