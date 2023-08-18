package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder
import platform.CoreBluetooth.CBCentralManagerScanOptionAllowDuplicatesKey
import platform.CoreBluetooth.CBCentralManagerScanOptionSolicitedServiceUUIDsKey
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSArray

public actual class ScannerBuilder {

    public actual var filters: List<Filter>? = null

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
    public var solicitedServiceUuids: List<Uuid>? = null//listOf(uuidFrom("23584b66-2c3c-09f2-cf3f-bc60bda79d4b")) //null

    private var logging: Logging = Logging()

    public actual fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    internal actual fun build(): PlatformScanner {
        val options = mutableMapOf<String, Any>()
        allowDuplicateKeys?.also {
            options[CBCentralManagerScanOptionAllowDuplicatesKey] = it
        }
        solicitedServiceUuids?.also {
            val cbArray = it.map { it.toCBUUID() }
//            val nsArray = NSArray()
            options[CBCentralManagerScanOptionSolicitedServiceUUIDsKey] = cbArray as NSArray //.toTypedArray()
        }

        return CentralManagerCoreBluetoothScanner(
            central = CentralManager.Default,
            filters = filters.orEmpty(),
            options = options.toMap(),
            logging = logging,
        )
    }
}
