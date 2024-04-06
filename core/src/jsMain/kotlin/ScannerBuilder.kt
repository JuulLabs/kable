package com.juul.kable

import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder

public actual class ScannerBuilder {

    @Deprecated(
        message = "Use predicates",
        replaceWith = ReplaceWith("predicates"),
        level = DeprecationLevel.WARNING,
    )
    public actual var filters: List<Filter>? = null

    public actual var predicates: FilterPredicateSetBuilder.() -> Unit = { }

    private var logging: Logging = Logging()

    public actual fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    internal actual fun build(): PlatformScanner = BluetoothWebBluetoothScanner(
        bluetooth = bluetooth,
        predicates = filters?.deprecatedListToGroup() ?: FilterPredicateSetBuilder().apply(predicates).build(),
        logging = logging,
    )
}
