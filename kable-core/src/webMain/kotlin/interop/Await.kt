package com.juul.kable.interop

import kotlin.js.JsAny
import kotlin.js.JsBoolean
import kotlin.js.JsNumber
import kotlin.js.JsString
import kotlin.js.Promise
import kotlin.js.toBoolean
import kotlin.js.toDouble

/** Wrapper around `kotlinx.coroutines.await` which exists in both js and wasm, but has no expect-actual. */
internal expect suspend fun <T : JsAny?> Promise<T>.await(): T

/** Syntax sugar for [await] that makes using native Kotlin types a little less verbose. */
internal suspend fun Promise<JsBoolean>.await(): Boolean = (await<JsAny>() as JsBoolean).toBoolean()

/** Syntax sugar for [await] that makes using native Kotlin types a little less verbose. */
internal suspend fun Promise<JsNumber>.await(): Double = (await<JsAny>() as JsNumber).toDouble()

/** Syntax sugar for [await] that makes using native Kotlin types a little less verbose. */
internal suspend fun Promise<JsString>.await(): String = (await<JsAny>() as JsString).toString()

/** Syntax sugar for [await] that makes using native Kotlin types a little less verbose. */
internal suspend fun Promise<Nothing?>.await(): Unit = await()
