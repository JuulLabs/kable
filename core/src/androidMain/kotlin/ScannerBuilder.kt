package com.juul.kable

import android.bluetooth.le.ScanSettings
import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder

public actual class ScannerBuilder {

    public actual var filters: List<Filter>? = null

    /**
     * Allows for the [Scanner] to be configured via Android's [ScanSettings].
     *
     * This property will be removed in a future version, and will be replaced by a Kable provided DSL for configuring
     * scanning.
     */
    @ObsoleteKableApi
    public var scanSettings: ScanSettings = ScanSettings.Builder().build()

    private var logging: Logging = Logging()

    /**
     * Allows for the [Scanner] to be configured to use either a blocking or non-blocking send
     * operation from within the callback flow. This is designed to provide an escape hatch
     * for users that run into threading issues on certain devices.
     */
    public var shouldUseBlockingSend: Boolean = true

    public actual fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    @OptIn(ObsoleteKableApi::class)
    internal actual fun build(): AndroidScanner = BluetoothLeScannerAndroidScanner(
        filters = filters.orEmpty(),
        scanSettings = scanSettings,
        logging = logging,
        shouldUseBlockingSend = shouldUseBlockingSend
    )
}
