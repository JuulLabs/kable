package com.juul.kable.interop

import kotlin.js.JsAny
import kotlin.js.JsBoolean
import kotlin.js.JsNumber
import kotlin.js.JsString
import kotlin.js.Promise
import kotlin.js.toBoolean
import kotlin.js.toDouble

/**
 * Workaround for regression in kotlinx.coroutines that fails to propagate Promise failures from JS as `JsException` when running on WASM.
 *
 * Problematic line: https://github.com/Kotlin/kotlinx.coroutines/blob/1.11.0/kotlinx-coroutines-core/wasmJs/src/Promise.wasm.kt#L11
 * Changed in: https://github.com/Kotlin/kotlinx.coroutines/pull/4563
 */
internal expect suspend fun <T : JsAny?> Promise<T>.await(): T

/** Syntax sugar for [await] that makes using native Kotlin types a little less verbose. */
internal suspend fun Promise<JsBoolean>.await(): Boolean = (await<JsAny>() as JsBoolean).toBoolean()

/** Syntax sugar for [await] that makes using native Kotlin types a little less verbose. */
internal suspend fun Promise<JsNumber>.await(): Double = (await<JsAny>() as JsNumber).toDouble()

/** Syntax sugar for [await] that makes using native Kotlin types a little less verbose. */
internal suspend fun Promise<JsString>.await(): String = (await<JsAny>() as JsString).toString()

/** Syntax sugar for [await] that makes using native Kotlin types a little less verbose. */
internal suspend fun Promise<Nothing?>.await(): Unit = await()
