package com.juul.kable

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

    /**
     * Filters [Advertisement]s during a scan. If predicates are non-empty, then only [Advertisement]s
     * that match at least one of the predicates are emitted during a scan.
     *
     * Filtering on Manufacturer Data and Service Data is passed through to the browser, and a good
     * explanation can be found here:
     * https://github.com/WebBluetoothCG/web-bluetooth/blob/main/data-filters-explainer.md
     *
     * Service Data filtering is subject to browser support; implementation status is tracked at:
     * https://github.com/WebBluetoothCG/web-bluetooth/blob/main/implementation-status.md
     */
    public actual fun filters(builderAction: FiltersBuilder.() -> Unit) {
        filterPredicates = FiltersBuilder().apply(builderAction).build()
    }

    private var logging: Logging = Logging()

    public actual fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    internal actual fun build(): PlatformScanner = BluetoothWebBluetoothScanner(
        filters = filterPredicates,
        logging = logging,
    )
}
