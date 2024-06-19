package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder
import platform.CoreBluetooth.CBCentralManagerScanOptionAllowDuplicatesKey
import platform.CoreBluetooth.CBCentralManagerScanOptionSolicitedServiceUUIDsKey

public actual class ScannerBuilder {

    @Deprecated(
        message = "Use filters(FilterPredicateSetBuilder.() -> Unit)",
        replaceWith = ReplaceWith("filters { }"),
        level = DeprecationLevel.WARNING,
    )
    public actual var filters: List<Filter>? = null

    private var filterPredicateSet: FilterPredicateSet = FilterPredicateSet()

    public actual fun filters(builderAction: FilterPredicateSetBuilder.() -> Unit) {
        filterPredicateSet = FilterPredicateSetBuilder().apply(builderAction).build()
    }

    /**
     * Specifies whether the scan should run without duplicate filtering. This corresponds to
     * Core Bluetooth's [CBCentralManagerScanOptionAllowDuplicatesKey] scanning option.
     */
    public var allowDuplicateKeys: Boolean? = null

    /**
     * Causes the scanner to scan for peripherals soliciting any of the services contained in the
     * array. This corresponds to Core Bluetooth's [CBCentralManagerScanOptionSolicitedServiceUUIDsKey]
     * scanning option.
     */
    public var solicitedServiceUuids: List<Uuid>? = null

    private var logging: Logging = Logging()

    public actual fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    internal actual fun build(): PlatformScanner {
        val options = mutableMapOf<String, Any>()
        allowDuplicateKeys?.also {
            options[CBCentralManagerScanOptionAllowDuplicatesKey] = it
        }
        solicitedServiceUuids?.also { uuids ->
            options[CBCentralManagerScanOptionSolicitedServiceUUIDsKey] = uuids.map(Uuid::toCBUUID)
        }

        return CentralManagerCoreBluetoothScanner(
            central = CentralManager.Default,
            filters = filters?.deprecatedListToPredicateSet() ?: filterPredicateSet,
            options = options.toMap(),
            logging = logging,
        )
    }
}
