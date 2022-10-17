package com.juul.kable

import com.juul.kable.logs.LoggingBuilder

public expect class ScannerBuilder internal constructor() {
    public var filters: List<Filter>?
    public fun logging(init: LoggingBuilder)
    internal fun build(): Scanner
}
