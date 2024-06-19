package com.juul.kable

import com.juul.kable.logs.LoggingBuilder

public actual class ScannerBuilder {

    @Deprecated(
        message = "Use filters(FilterPredicateSetBuilder.() -> Unit)",
        replaceWith = ReplaceWith("filters { }"),
        level = DeprecationLevel.WARNING,
    )
    public actual var filters: List<Filter>? = null

    public actual fun filters(builderAction: FilterPredicateSetBuilder.() -> Unit) {
        jvmNotImplementedException()
    }

    public actual fun logging(init: LoggingBuilder) {
        jvmNotImplementedException()
    }

    internal actual fun build(): PlatformScanner = jvmNotImplementedException()
}
