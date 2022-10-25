package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.CentralManagerDelegate.Response.DidDiscoverPeripheral
import com.juul.kable.logs.Logging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import platform.CoreBluetooth.CBCentralManagerScanOptionAllowDuplicatesKey
import platform.CoreBluetooth.CBCentralManagerScanOptionSolicitedServiceUUIDsKey
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBManagerStateUnauthorized
import platform.CoreBluetooth.CBManagerStateUnsupported

public class AppleScanner internal constructor(
    central: CentralManager,
    services: List<Uuid>?,
    private val options: ScanOptions?,
    logging: Logging,
) : Scanner {

    public override val advertisements: Flow<Advertisement> =
        central.delegate
            .response
            .onStart {
                central.awaitPoweredOn()
                central.scanForPeripheralsWithServices(services, options = appleOptions())
            }
            .onCompletion {
                central.stopScan()
            }
            .filterIsInstance<DidDiscoverPeripheral>()
            .map { (cbPeripheral, rssi, advertisementData) ->
                Advertisement(rssi.intValue, advertisementData, cbPeripheral)
            }

    private fun appleOptions(): Map<Any?, *>? =
        if (options != null) {
            mapOf(
                CBCentralManagerScanOptionAllowDuplicatesKey to options.allowDuplicateKeys,
                CBCentralManagerScanOptionSolicitedServiceUUIDsKey to options.solicitedServiceUuids,
            )
        } else {
            null
        }

    /**
     * Scanning options for Apple's CBCentralManager.scanForPeripherals(withServices:options:).
     * This enables peripheral scanning options such as CBCentralManagerScanOptionAllowDuplicatesKey
     * to be specified when scanning begins.
     */
    public data class ScanOptions(
        /**
         * Specifies whether the scan should run without duplicate filtering. Default is false.
         */
        val allowDuplicateKeys: Boolean = false,

        /**
         * Causes the scanner to scan for peripherals soliciting any of the services contained
         * in the array.
         */
        val solicitedServiceUuids: List<Uuid> = emptyList(),
    )
}

private suspend fun CentralManager.awaitPoweredOn() {
    delegate.state
        .onEach {
            if (it == CBManagerStateUnsupported ||
                it == CBManagerStateUnauthorized
            ) {
                error("Invalid bluetooth state: $it")
            }
        }
        .first { it == CBManagerStatePoweredOn }
}
