package com.juul.kable

import com.juul.kable.logs.LoggingBuilder

public expect class ScannerBuilder internal constructor() {
    public var filters: List<Filter>?
    public fun logging(init: LoggingBuilder)

    /**
     * Scanning options for platforms that support options (i.e. iOS's CBCentralManager)
     */
    public var scanOptions: Map<Any?, *>?

    internal fun build(): Scanner
}
