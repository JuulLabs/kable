@file:OptIn(ExperimentalUuidApi::class)

package com.juul.kable

import com.juul.kable.CentralManagerDelegate.Response.DidDiscoverPeripheral
import com.juul.kable.Filter.Service
import com.juul.kable.UnmetRequirementReason.BluetoothDisabled
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import platform.CoreBluetooth.CBManagerStatePoweredOff
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBManagerStateUnauthorized
import platform.CoreBluetooth.CBManagerStateUnsupported
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class CentralManagerCoreBluetoothScanner(
    central: CentralManager,
    filters: List<FilterPredicate>,
    options: Map<Any?, *>?,
    logging: Logging,
) : PlatformScanner {

    init {
        require(filters.flatten().none { it is Filter.Address }) {
            "Filtering by address (`Filter.Address`) is not supported on Apple platforms"
        }
    }

    private val logger = Logger(logging, tag = "Kable/Scanner", identifier = null)

    private val nativeServiceFilters = filters.toNativeServiceFilter()

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
                logger.verbose { message = "Initializing scan" }
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
                val advertisementData = didDiscoverPeripheral.advertisementData.asAdvertisementData()
                filters.matches(
                    services = advertisementData.serviceUuids,
                    name = advertisementData.localName,
                    manufacturerData = advertisementData.manufacturerData,
                )
            }
            .map { (cbPeripheral, rssi, advertisementData) ->
                CBPeripheralCoreBluetoothAdvertisement(rssi.intValue, advertisementData, cbPeripheral)
            }
}

private suspend fun CentralManager.awaitPoweredOn() {
    delegate.state
        .onEach { state ->
            when (state) {
                CBManagerStateUnsupported -> error("This device doesn't support the Bluetooth low energy central or client role")
                CBManagerStateUnauthorized -> error("Application isn't authorized to use the Bluetooth low energy role")
                CBManagerStatePoweredOff -> throw UnmetRequirementException(BluetoothDisabled, "Bluetooth disabled")
            }
        }
        .first { it == CBManagerStatePoweredOn }
}

// Native filtering of advertisements can only be performed if each predicate set contains a `Filter.Service`.
private fun List<FilterPredicate>.supportsNativeServiceFiltering(): Boolean =
    all { predicate ->
        predicate.filters.any { it is Service }
    }

// Note that we unroll them all into a flat list which then behaves as a "pre-filter" that lets
// the system filter for advertisements that match _any_ of the services we provide. This is
// desirable on mobile for efficiency. We still need to apply our matching logic afterwards as
// the unrolling process necessarily discards any compound clauses that the filters may contain,
// along with any non-service filters that may be in there.
private fun List<FilterPredicate>.toNativeServiceFilter(): List<Uuid>? =
    if (supportsNativeServiceFiltering()) {
        flatMap(FilterPredicate::filters)
            .filterIsInstance<Service>()
            .map(Service::uuid)
            .ifEmpty { null }
    } else {
        null
    }
