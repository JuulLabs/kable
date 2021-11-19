package com.juul.kable

import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder

public actual class ScannerBuilder {
    public actual var filters: List<Filter>? = null
    private var logging: Logging = Logging()

    public actual fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    internal actual fun build(): Scanner {
        val filters = filters
            ?.map { it.toString() }
            ?.filterIsInstance<Filter.Service>()
            ?.map { it.uuid.toString() }
            ?.toTypedArray()
            ?.let { arrayOf<Options.Filter>(Options.Filter.Services(it)) }
        return JsScanner(
            bluetooth = bluetooth,
            options = Options(filters = filters),
            logging = logging,
        )
    }
}
