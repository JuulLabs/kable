package com.juul.kable.external

internal external interface JsIterator<T> {
    fun next(): JsIteratorResult<T>
}

internal external class JsIteratorResult<T> {
    val done: Boolean
    val value: T?
}

internal fun <T> JsIterator<T>.iterable(): Iterable<T> {
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
