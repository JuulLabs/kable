package com.juul.kable

import com.juul.kable.logs.LoggingBuilder

public expect class ScannerBuilder internal constructor() {

    /**
     * Filters [Advertisement]s during a scan: If [filters] is `null` or empty, then no filtering is performed (i.e. all
     * [Advertisement]s are emitted during a scan). If filters are provided (i.e. [filters] is a list of at least one
     * [Filter]), then only [Advertisement]s that match at least one [Filter] are emitted during a scan.
     */
    @Deprecated(
        message = "Use filters(FilterPredicateSetBuilder.() -> Unit)",
        replaceWith = ReplaceWith("filters { }"),
        level = DeprecationLevel.WARNING,
    )
    public var filters: List<Filter>?

    /**
     * Filters [Advertisement]s during a scan. If predicates are non-empty, then only [Advertisement]s
     * that match at least one of the predicates are emitted during a scan.
     */
    public fun filters(builderAction: FilterPredicateSetBuilder.() -> Unit)

    public fun logging(init: LoggingBuilder)
    internal fun build(): PlatformScanner
}

// To preserve original behavior make each individual filter a separate predicate:
internal fun List<Filter>.deprecatedListToGroup(): FilterPredicateSet =
    FilterPredicateSet(map { filter -> FilterPredicate(listOf(filter)) })
