package com.juul.kable

import com.benasher44.uuid.Uuid
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
    predicates: FilterPredicateSet,
    options: Map<Any?, *>?,
    logging: Logging,
) : PlatformScanner {

    init {
        require(predicates.flatten().none { it is Filter.Address }) {
            "Filtering by address (`Filter.Address`) is not supported on Apple platforms"
        }
    }

    private val logger = Logger(logging, tag = "Kable/Scanner", identifier = null)

    private val nativeServiceFilters = predicates.toNativeServiceFilter()

    init {
        if (nativeServiceFilters == null) {
            logger.warn {
                @Suppress("ktlint:standard:max-line-length")
                message = "According to Core Bluetooth documentation: " +
                    "\"The recommended practice is to populate the serviceUUIDs parameter rather than leaving it nil.\" " +
                    "This means providing a non-empty `services` member on every Scanner predicate. " +
                    "See https://developer.apple.com/documentation/corebluetooth/cbcentralmanager/1518986-scanforperipheralswithservices#discussion for more details."
            }
        }
    }

    override val advertisements: Flow<PlatformAdvertisement> =
        central.delegate
            .response
            .onStart {
                central.awaitPoweredOn()
                if (nativeServiceFilters != null) {
                    logger.info { message = "Starting scan with native service filtering" }
                    central.scanForPeripheralsWithServices(nativeServiceFilters, options)
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
                // Short-circuit (i.e. don't filter) if no filters were provided.
                if (predicates.isEmpty()) return@filter true

                val advertisementData = didDiscoverPeripheral.advertisementData.asAdvertisementData()
                predicates.matches(
                    services = advertisementData.serviceUuids,
                    name = advertisementData.localName,
                    manufacturerData = advertisementData.manufacturerData,
                )
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

private fun FilterPredicateSet.flatten(): List<Filter> =
    predicates.flatMap(FilterPredicate::filters)

// Native filtering of advertisements is performed if each predicate set contains a `Filter.Service`.
private fun FilterPredicateSet.supportsNativeServiceFiltering(): Boolean =
    predicates.all { predicate ->
        predicate.filters.any { it is Filter.Service }
    }

private fun FilterPredicateSet.toNativeServiceFilter(): List<Uuid>? =
    if (supportsNativeServiceFiltering()) {
        predicates.flatMap(FilterPredicate::filters)
            .filterIsInstance<Filter.Service>()
            .map(Filter.Service::uuid)
    } else {
        null
    }
