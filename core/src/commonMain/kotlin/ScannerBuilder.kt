package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.logs.LoggingBuilder

public expect class ScannerBuilder internal constructor() {
    public var services: List<Uuid>?
    public fun logging(init: LoggingBuilder)
    internal fun build(): Scanner
}
