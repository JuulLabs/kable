package com.juul.kable

import com.juul.kable.CentralManagerDelegate.Response.DidDiscoverPeripheral
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.cinterop.UnsafeNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBManagerStateUnauthorized
import platform.CoreBluetooth.CBManagerStateUnsupported

internal class CentralManagerCoreBluetoothScanner(
    central: CentralManager,
    filters: List<Filter>,
    options: Map<Any?, *>?,
    logging: Logging,
) : CoreBluetoothScanner {

    init {
        require(filters.none { it is Filter.Address }) {
            "Filtering by address (`Filter.Address`) is not supported on Apple platforms"
        }
    }

    private val logger = Logger(logging, tag = "Kable/Scanner", identifier = null)

    private val serviceFilters = filters.filterIsInstance<Filter.Service>().map(Filter.Service::uuid)

    // Native filtering of advertisements is performed if: `filters` contains only (and at least one) `Filter.Service`.
    private val nativeServiceFiltering = filters.isNotEmpty() && filters.all { it is Filter.Service }

    init {
        if (!nativeServiceFiltering) {
            logger.warn {
                message = "According to Core Bluetooth documentation: " +
                    "\"The recommended practice is to populate the serviceUUIDs parameter rather than leaving it nil.\" " +
                    "This means providing only (and at least 1) filter(s) of type `Filter.Service` to Scanner. " +
                    "See https://developer.apple.com/documentation/corebluetooth/cbcentralmanager/1518986-scanforperipheralswithservices#discussion for more details."
            }
        }
    }

    override val advertisements: Flow<CoreBluetoothAdvertisement> =
        central.delegate
            .response
            .onStart {
                central.awaitPoweredOn()
                if (nativeServiceFiltering) {
                    logger.info { message = "Starting scan with native service filtering" }
                    central.scanForPeripheralsWithServices(serviceFilters, options)
                } else {
                    logger.info { message = "Starting scan with non-native filtering" }
                    central.scanForPeripheralsWithServices(null, options)
                }
            }
            .onCompletion {
                logger.info { message = "Stopping scan" }
                central.stopScan()
            }
            .filterIsInstance<DidDiscoverPeripheral>()
            .filter { didDiscoverPeripheral ->
                if (nativeServiceFiltering) return@filter true // Short-circuit (i.e. don't filter) when scan is using native service filtering.
                if (filters.isEmpty()) return@filter true // Short-circuit (i.e. don't filter) if no filters were provided.

                val advertisementData = didDiscoverPeripheral.advertisementData.asAdvertisementData()
                filters.any { filter ->
                    when (filter) {
                        is Filter.Service -> filter.matches(advertisementData.serviceUuids)
                        is Filter.Name -> filter.matches(advertisementData.localName)
                        is Filter.NamePrefix -> filter.matches(advertisementData.localName)
                        is Filter.ManufacturerData -> filter.matches(advertisementData.manufacturerData?.data)
                        is Filter.Address -> throw UnsupportedOperationException("Filtering by address is not supported on Apple platforms")
                    }
                }
            }
            .map { (cbPeripheral, rssi, advertisementData) ->
                CBPeripheralCoreBluetoothAdvertisement(rssi.intValue, advertisementData, cbPeripheral)
            }
}

@OptIn(UnsafeNumber::class)
private suspend fun CentralManager.awaitPoweredOn() {
    delegate.state
        .onEach {
            if (it == CBManagerStateUnsupported || it == CBManagerStateUnauthorized) {
                error("Invalid bluetooth state: $it")
            }
        }
        .first { it == CBManagerStatePoweredOn }
}
