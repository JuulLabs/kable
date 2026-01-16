package com.juul.kable.external

import kotlin.js.JsAny

internal external interface JsIterator<T : JsAny?> : JsAny {
    fun next(): JsIteratorResult<T>
}

internal external interface JsIteratorResult<T : JsAny?> : JsAny {
    val done: Boolean
    val value: T?
}

internal fun <T : JsAny?> JsIterator<T>.iterable(): Iterable<T> {
    return object : Iterable<T> {
        override fun iterator(): Iterator<T> =
            object : Iterator<T> {
                private var nextElement = this@iterable.next()
                override fun hasNext() = !nextElement.done
                override fun next(): T {
                    val value = nextElement.value ?: error("No more values")
                    nextElement = this@iterable.next()
                    return value
                }
            }
    }
}
