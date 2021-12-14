package com.juul.kable

import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder

public actual class ScannerBuilder {
    @Deprecated(message = "Replaced by filters property")
    public var services: List<Uuid>?
        set(value) {
            filters = value?.map { Filter.Service(it) }
        }
        get() = filters?.filterIsInstance<Filter.Service>()?.map { it.uuid }

    public actual var filters: List<Filter>? = null
    private var logging: Logging = Logging()

    public actual fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    internal actual fun build(): Scanner = AppleScanner(
        central = CentralManager.Default,
        services = filters?.filterIsInstance<Filter.Service>()?.map { it.uuid },
        logging = logging,
    )
}
