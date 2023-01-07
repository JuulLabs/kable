package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder

public actual class ScannerBuilder {
    @Deprecated(
        message = "Replaced by filters property",
        level = DeprecationLevel.HIDDEN,
    )
    public var services: List<Uuid>?
        set(value) { throw UnsupportedOperationException() }
        get() = throw UnsupportedOperationException()

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
