package com.juul.kable

import com.juul.kable.logs.LoggingBuilder

public actual class ScannerBuilder {

    @Deprecated(
        message = "Use filters(FiltersBuilder.() -> Unit)",
        replaceWith = ReplaceWith("filters { }"),
        level = DeprecationLevel.WARNING,
    )
    public actual var filters: List<Filter>? = null

    public actual fun filters(builderAction: FiltersBuilder.() -> Unit) {
        jvmNotImplementedException()
    }

    public actual fun logging(init: LoggingBuilder) {
        jvmNotImplementedException()
    }

    internal actual fun build(): PlatformScanner = jvmNotImplementedException()
}
