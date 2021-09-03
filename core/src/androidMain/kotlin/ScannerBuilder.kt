package com.juul.kable

import android.bluetooth.le.ScanSettings
import com.benasher44.uuid.Uuid
import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder

public actual class ScannerBuilder {
    public actual var services: List<Uuid>? = null
    public var scanSettings: ScanSettings = ScanSettings.Builder().build()
    private var logging: Logging = Logging()

    public actual fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    internal actual fun build(): Scanner = AndroidScanner(
        filterServices = services,
        scanSettings = scanSettings,
        logging = logging,
    )
}
