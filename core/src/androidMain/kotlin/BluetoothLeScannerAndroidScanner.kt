package com.juul.kable

import android.bluetooth.BluetoothAdapter.STATE_ON
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

internal class BluetoothLeScannerAndroidScanner(
    private val filters: List<Filter>,
    private val scanSettings: ScanSettings,
    private val preConflate: Boolean,
    logging: Logging,
) : AndroidScanner {

    private val logger = Logger(logging, tag = "Kable/Scanner", identifier = null)

    private val namePrefixFilters = filters.filterIsInstance<NamePrefix>()

    override val advertisements: Flow<AndroidAdvertisement> = callbackFlow {
        val scanner = getBluetoothAdapter().bluetoothLeScanner ?: throw BluetoothDisabledException()

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
            message = logMessage("Starting", preConflate, scanFilters)
        }
        checkBluetoothAdapterState(STATE_ON)
        scanner.startScan(scanFilters, scanSettings, callback)

        awaitClose {
            logger.info {
                message = logMessage("Stopping", preConflate, scanFilters)
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

private fun logMessage(prefix: String, preConflate: Boolean, scanFilters: List<ScanFilter>) = buildString {
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
