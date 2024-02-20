package com.juul.kable

import com.juul.kable.logs.LoggingBuilder

public actual class ScannerBuilder {

    public actual var filters: List<Filter>? = null

    public actual fun logging(init: LoggingBuilder) {
        jvmNotImplementedException()
    }

    internal actual fun build(): PlatformScanner = jvmNotImplementedException()
}
