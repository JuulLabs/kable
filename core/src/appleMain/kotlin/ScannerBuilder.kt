package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder
import platform.CoreBluetooth.CBCentralManagerScanOptionAllowDuplicatesKey
import platform.CoreBluetooth.CBCentralManagerScanOptionSolicitedServiceUUIDsKey

public actual class ScannerBuilder {
    @Deprecated(
        message = "Replaced by filters property",
        level = DeprecationLevel.ERROR,
    )
    public var services: List<Uuid>?
        set(value) {
            filters = value?.map { Filter.Service(it) }
        }
        get() = filters?.filterIsInstance<Filter.Service>()?.map { it.uuid }

    public actual var filters: List<Filter>? = null

    /**
     * Specifies whether the scan should run without duplicate filtering. This corresponds to
     * Core Bluetooth's [CBCentralManagerScanOptionAllowDuplicatesKey] scanning option.
     */
    public var allowDuplicateKeys: Boolean? = null

    /**
     * Causes the scanner to scan for peripherals soliciting any of the services contained in the
     * array. This corresponds to Apple's CBCentralManagerScanOptionSolicitedServiceUUIDsKey
     * scanning option.
     */
    public var solicitedServiceUuids: List<Uuid>? = null

    private var logging: Logging = Logging()

    public actual fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    internal actual fun build(): Scanner {
        val options = mutableMapOf<String, Any>()
        allowDuplicateKeys?.also {
            options[CBCentralManagerScanOptionAllowDuplicatesKey] = it
        }
        solicitedServiceUuids?.also {
            options[CBCentralManagerScanOptionSolicitedServiceUUIDsKey] = it.toTypedArray()
        }

        return AppleScanner(
            central = CentralManager.Default,
            services = filters?.filterIsInstance<Filter.Service>()?.map { it.uuid },
            options = options.toMap(),
            logging = logging,
        )
    }
}
