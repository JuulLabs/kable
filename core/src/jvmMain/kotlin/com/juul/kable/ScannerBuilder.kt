package com.juul.kable

import com.juul.kable.logs.LoggingBuilder

public actual class ScannerBuilder {

    @Deprecated(
        message = "Use predicates",
        replaceWith = ReplaceWith("predicates"),
        level = DeprecationLevel.WARNING,
    )
    public actual var filters: List<Filter>? = null

    public actual var predicates: FilterPredicateSetBuilder.() -> Unit = { }

    public actual fun logging(init: LoggingBuilder) {
        jvmNotImplementedException()
    }

    internal actual fun build(): PlatformScanner = jvmNotImplementedException()
}
