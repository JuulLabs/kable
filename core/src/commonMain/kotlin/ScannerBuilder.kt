package com.juul.kable

import com.juul.kable.logs.LoggingBuilder

public expect class ScannerBuilder internal constructor() {

    /**
     * Filters [Advertisement]s during a scan: If [filters] is `null` or empty, then no filtering is performed (i.e. all
     * [Advertisement]s are emitted during a scan). If filters are provided (i.e. [filters] is a list of at least one
     * [Filter]), then only [Advertisement]s that match at least one [Filter] are emitted during a scan.
     */
    public var filters: List<Filter>?

    public fun logging(init: LoggingBuilder)
    internal fun build(): Scanner
}
