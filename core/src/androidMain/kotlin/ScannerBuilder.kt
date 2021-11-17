package com.juul.kable

import android.bluetooth.le.ScanSettings
import com.benasher44.uuid.Uuid
import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder

public actual class ScannerBuilder {
    public var manufacturerDataFilters: List<ManufacturerDataFilter>? = null
    public actual var services: List<Uuid>? = null

    /**
     * Allows for the [Scanner] to be configured via Android's [ScanSettings].
     *
     * This property will be removed in a future version, and will be replaced by a Kable provided DSL for configuring
     * scanning.
     */
    @ObsoleteKableApi
    public var scanSettings: ScanSettings = ScanSettings.Builder().build()

    private var logging: Logging = Logging()

    public actual fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    internal actual fun build(): Scanner = AndroidScanner(
        filterServices = services,
        manufacturerDataFilters = manufacturerDataFilters,
        scanSettings = scanSettings,
        logging = logging,
    )
}
