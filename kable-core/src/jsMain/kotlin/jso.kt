@file:Suppress("NOTHING_TO_INLINE")

package com.juul.kable

// Copied from:
// https://github.com/JetBrains/kotlin-wrappers/blob/8a118e3261c093cf1b88002502ca36f17d356394/kotlin-js/src/main/kotlin/js/core/jso.kt
internal inline fun <T : Any> jso(): T = js("({})")
internal inline fun <T : Any> jso(block: T.() -> Unit): T = jso<T>().apply(block)
