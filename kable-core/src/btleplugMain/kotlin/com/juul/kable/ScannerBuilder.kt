package com.juul.kable

import com.juul.kable.btleplug.BtleplugScanner
import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder

public actual class ScannerBuilder {

    @Deprecated(
        message = "Use filters(FiltersBuilder.() -> Unit)",
        replaceWith = ReplaceWith("filters { }"),
        level = DeprecationLevel.HIDDEN,
    )
    public actual var filters: List<Filter>? = null

    private var filterPredicates: List<FilterPredicate> = emptyList()

    public actual fun filters(builderAction: FiltersBuilder.() -> Unit) {
        filterPredicates = FiltersBuilder().apply(builderAction).build()
    }

    private var logging: Logging = Logging()

    public actual fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    internal actual fun build(): PlatformScanner = BtleplugScanner(filterPredicates, logging)
}
