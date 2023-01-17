package com.juul.kable

import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder

public actual class ScannerBuilder {

    public actual var filters: List<Filter>? = null

    private var logging: Logging = Logging()

    public actual fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    internal actual fun build(): Scanner = JsScanner(
        bluetooth = bluetooth,
        filters = filters.orEmpty(),
        logging = logging,
    )
}
