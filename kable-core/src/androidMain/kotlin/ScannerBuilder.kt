package com.juul.kable

import android.bluetooth.le.ScanSettings
import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate

public actual class ScannerBuilder {

    @Deprecated(
        message = "Use filters(FiltersBuilder.() -> Unit)",
        replaceWith = ReplaceWith("filters { }"),
        level = DeprecationLevel.HIDDEN,
    )
    public actual var filters: List<Filter>? = null

    private var filterPredicates: List<FilterPredicate> = emptyList()

    public actual fun filters(builderAction: FiltersBuilder.() -> Unit) {
        filterPredicates = FiltersBuilder().apply(builderAction).build()
    }

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
     * Configures how many [advertisements][Scanner.advertisements] may be buffered while waiting
     * to be collected.
     *
     * Scan results are never delivered by blocking the (Android provided) thread that scan
     * callbacks are invoked from, so this capacity — rather than backpressure — is what absorbs a
     * collector slower than scan results arrive:
     *
     * - [UNLIMITED] (the default) buffers without bound, so no scan results are dropped.
     * - Any capacity of at least `1` drops the oldest buffered scan result to make room for the
     *   newest. A capacity of `1` is equivalent to applying the [conflate][Flow.conflate] flow
     *   operator on the [advertisements][Scanner.advertisements] property.
     *
     * `Channel.BUFFERED` and `Channel.CONFLATED` are rejected, as neither keeps its usual meaning
     * once the oldest buffered scan result is dropped on overflow: `Channel.BUFFERED` would
     * silently behave as a capacity of `1` rather than the default channel capacity, and
     * `Channel.CONFLATED` cannot be combined with an overflow strategy at all.
     *
     * See https://github.com/JuulLabs/kable/issues/654 for more details.
     */
    public var bufferCapacity: Int = UNLIMITED
        set(value) {
            require(value >= 1) {
                "Buffer capacity must be at least 1 (UNLIMITED for no limit), but was $value"
            }
            field = value
        }

    /**
     * Configures [Scanner] to pre-conflate the [advertisements][Scanner.advertisements] flow.
     *
     * See https://github.com/JuulLabs/kable/issues/654 for more details.
     */
    @Deprecated(
        message = "Use bufferCapacity, where a capacity of 1 conflates and UNLIMITED (the default) does not drop.",
        replaceWith = ReplaceWith("bufferCapacity = 1"),
    )
    public var preConflate: Boolean
        get() = bufferCapacity != UNLIMITED
        set(value) {
            bufferCapacity = if (value) 1 else UNLIMITED
        }

    public actual fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    @OptIn(ObsoleteKableApi::class)
    internal actual fun build(): PlatformScanner = BluetoothLeScannerAndroidScanner(
        filters = filterPredicates,
        scanSettings = scanSettings,
        logging = logging,
        bufferCapacity = bufferCapacity,
    )
}
