package com.juul.kable

import android.bluetooth.le.ScanSettings
import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.runBlocking

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
     * Configures [Scanner] to pre-conflate the [advertisements][Scanner.advertisements] flow.
     *
     * Roughly equivalent to applying the [conflate][Flow.conflate] flow operator on the
     * [advertisements][Scanner.advertisements] property (but without [runBlocking] overhead).
     *
     * May prevent ANRs on some Android phones (observed on specific Samsung models) that have
     * delicate binder threads.
     *
     * See https://github.com/JuulLabs/kable/issues/654 for more details.
     */
    public var preConflate: Boolean = false

    public actual fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    @OptIn(ObsoleteKableApi::class)
    internal actual fun build(): PlatformScanner = BluetoothLeScannerAndroidScanner(
        filters = filters.orEmpty(),
        scanSettings = scanSettings,
        logging = logging,
        preConflate = preConflate,
    )
}
