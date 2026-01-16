package com.juul.kable

val isBrowser: Boolean
    get() = js("typeof window !== 'undefined'")
        .unsafeCast<Boolean>()

val isNode: Boolean
    get() = js("typeof process !== 'undefined' && process.versions && process.versions.node")
        .unsafeCast<Boolean>()
