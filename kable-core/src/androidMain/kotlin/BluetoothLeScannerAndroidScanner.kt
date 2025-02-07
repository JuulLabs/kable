package com.juul.kable

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM
import android.os.ParcelUuid
import com.juul.kable.Filter.Address
import com.juul.kable.Filter.ManufacturerData
import com.juul.kable.Filter.Name
import com.juul.kable.Filter.Service
import com.juul.kable.bluetooth.checkBluetoothIsOn
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.scan.ScanError
import com.juul.kable.scan.message
import com.juul.kable.scan.requirements.checkLocationServicesEnabled
import com.juul.kable.scan.requirements.checkScanPermissions
import com.juul.kable.scan.requirements.requireBluetoothLeScanner
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlin.uuid.toJavaUuid

internal class BluetoothLeScannerAndroidScanner(
    private val filters: List<FilterPredicate>,
    private val scanSettings: ScanSettings,
    private val preConflate: Boolean,
    logging: Logging,
) : PlatformScanner {

    private val logger = Logger(logging, tag = "Kable/Scanner", identifier = null)

    private val scanFilters = filters.toNativeScanFilters()

    override val advertisements: Flow<PlatformAdvertisement> = callbackFlow {
        logger.debug { message = "Initializing scan" }
        val scanner = requireBluetoothLeScanner()

        // Permissions are checked early (fail-fast), as they cannot be unexpectedly revoked prior
        // to scanning (revoking permissions on Android restarts the app).
        logger.verbose { message = "Checking permissions for scanning" }
        checkScanPermissions()

        fun sendResult(scanResult: ScanResult) {
            val advertisement = ScanResultAndroidAdvertisement(scanResult)
            when {
                preConflate -> trySend(advertisement)
                else -> trySendBlocking(advertisement)
            }.onFailure {
                logger.warn { message = "Unable to deliver scan result due to failure in flow or premature closing." }
            }
        }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                sendResult(result)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                results.forEach(::sendResult)
            }

            override fun onScanFailed(errorCode: Int) {
                val scanError = ScanError(errorCode)
                logger.error {
                    detail("code", scanError.toString())
                    message = "Scan could not be started"
                }
                close(IllegalStateException(scanError.message))
            }
        }

        // These conditions could change prior to scanning, so we check them as close to
        // initiating the scan as feasible.
        logger.verbose { message = "Checking scanning requirements" }
        checkLocationServicesEnabled()
        checkBluetoothIsOn()

        logger.info {
            message = logMessage("Starting", preConflate, scanFilters)
        }
        scanner.startScan(scanFilters, scanSettings, callback)

        awaitClose {
            logger.info {
                message = logMessage("Stopping", preConflate, scanFilters)
            }
            // Can't check BLE state here, only Bluetooth, but should assume `IllegalStateException`
            // means BLE has been disabled.
            try {
                scanner.stopScan(callback)
            } catch (e: IllegalStateException) {
                logger.warn(e) { message = "Failed to stop scan. " }
            }
        }
    }.filter { advertisement ->
        // Short-circuit (i.e. don't filter) if native scan filters were applied.
        if (scanFilters.isNotEmpty()) return@filter true

        // Perform filtering here, since we were not able to use native scan filters.
        filters.matches(
            services = advertisement.uuids,
            name = advertisement.name,
            address = advertisement.address,
            manufacturerData = advertisement.manufacturerData,
        )
    }
}

private fun logMessage(
    prefix: String,
    preConflate: Boolean,
    scanFilters: List<ScanFilter>,
) = buildString {
    append(prefix)
    append(' ')
    append("scan ")
    if (preConflate) {
        append("pre-conflated ")
    }
    if (scanFilters.isEmpty()) {
        append("without filters")
    } else {
        append("with ${scanFilters.size} filter(s)")
    }
}

private fun List<FilterPredicate>.toNativeScanFilters(): List<ScanFilter> =
    if (all(FilterPredicate::supportsNativeScanFiltering)) {
        map(FilterPredicate::toNativeScanFilter)
    } else {
        emptyList()
    }

private fun FilterPredicate.toNativeScanFilter(): ScanFilter =
    ScanFilter.Builder().apply {
        filters.map { filter ->
            when (filter) {
                is Name.Exact -> setDeviceName(filter.exact)
                is Address -> setDeviceAddress(filter.address)
                is ManufacturerData -> setManufacturerData(filter.id, filterDataCompat(filter.data), filter.dataMask)
                is Service -> setServiceUuid(ParcelUuid(filter.uuid.toJavaUuid()))
                else -> throw AssertionError("Unsupported filter element")
            }
        }
    }.build()

// Scan filter does not support name prefix filtering, and only allows at most one service uuid
// and one manufacturer data.
private fun FilterPredicate.supportsNativeScanFiltering(): Boolean =
    !containsNamePrefix() && serviceCount() <= 1 && manufacturerDataCount() <= 1

private fun FilterPredicate.containsNamePrefix(): Boolean =
    filters.any { it is Name.Prefix }

private fun FilterPredicate.serviceCount(): Int =
    filters.count { it is Service }

private fun FilterPredicate.manufacturerDataCount(): Int =
    filters.count { it is ManufacturerData }

// Android doesn't properly check for nullness of manufacturer data until Android 16.
// See https://github.com/JuulLabs/kable/issues/854 for more details.
private fun filterDataCompat(data: ByteArray?): ByteArray? =
    if (data == null && SDK_INT <= VANILLA_ICE_CREAM) byteArrayOf() else data
