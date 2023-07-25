package com.juul.kable

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import com.juul.kable.Filter.Address
import com.juul.kable.Filter.ManufacturerData
import com.juul.kable.Filter.Name
import com.juul.kable.Filter.NamePrefix
import com.juul.kable.Filter.Service
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter

public class ScanFailedException internal constructor(
    public val errorCode: Int,
) : IllegalStateException("Bluetooth scan failed with error code $errorCode")

internal class BluetoothLeScannerAndroidScanner(
    private val filters: List<Filter>,
    private val scanSettings: ScanSettings,
    logging: Logging,
) : AndroidScanner {

    private val logger = Logger(logging, tag = "Kable/Scanner", identifier = null)

    private val namePrefixFilters = filters.filterIsInstance<NamePrefix>()

    override val advertisements: Flow<AndroidAdvertisement> = callbackFlow {
        val scanner = getBluetoothAdapter().bluetoothLeScanner ?: throw BluetoothDisabledException()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySendBlocking(ScanResultAndroidAdvertisement(result))
                    .onFailure {
                        logger.warn { message = "Unable to deliver scan result due to failure in flow or premature closing." }
                    }
            }

            @SuppressLint("NewApi") // `forEach` incorrectly showing as minimum API 24 despite the Kotlin stdlib version being used.
            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                runCatching {
                    results.forEach {
                        trySendBlocking(ScanResultAndroidAdvertisement(it)).getOrThrow()
                    }
                }.onFailure {
                    logger.warn { message = "Unable to deliver batch scan results due to failure in flow or premature closing." }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                logger.error { message = "Scan could not be started, error code $errorCode." }
                cancel("Bluetooth scan failed", ScanFailedException(errorCode))
            }
        }

        val scanFilters = filters.map { filter ->
            ScanFilter.Builder().apply {
                when (filter) {
                    is Name -> setDeviceName(filter.name)
                    is NamePrefix -> {} // No-op: Filtering performed via flow.
                    is Address -> setDeviceAddress(filter.address)
                    is ManufacturerData -> setManufacturerData(filter.id, filter.data, filter.dataMask)
                    is Service -> setServiceUuid(ParcelUuid(filter.uuid)).build()
                }
            }.build()
        }

        logger.info {
            message = if (scanFilters.isEmpty()) {
                "Starting scan without filters"
            } else {
                "Starting scan with ${scanFilters.size} filter(s)"
            }
        }
        scanner.startScan(scanFilters, scanSettings, callback)

        awaitClose {
            logger.info {
                message = if (scanFilters.isEmpty()) {
                    "Stopping scan without filters"
                } else {
                    "Stopping scan with ${scanFilters.size} filter(s)"
                }
            }
            // Can't check BLE state here, only Bluetooth, but should assume `IllegalStateException` means BLE has been disabled.
            try {
                scanner.stopScan(callback)
            } catch (e: IllegalStateException) {
                logger.warn(e) { message = "Failed to stop scan. " }
            }
        }
    }.filter { advertisement ->
        // Short-circuit (i.e. don't filter) if no `Filter.NamePrefix` filters were provided.
        if (namePrefixFilters.isEmpty()) return@filter true

        // Perform `Filter.NamePrefix` filtering here, since it isn't supported natively.
        namePrefixFilters.any { filter -> filter.matches(advertisement.name) }
    }
}
