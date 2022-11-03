package com.juul.kable

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import com.juul.kable.Filter.DeviceInfo
import com.juul.kable.Filter.ManufacturerData
import com.juul.kable.Filter.Service
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

public class ScanFailedException internal constructor(
    public val errorCode: Int,
) : IllegalStateException("Bluetooth scan failed with error code $errorCode")

public class AndroidScanner internal constructor(
    private val filters: List<Filter>?,
    private val scanSettings: ScanSettings,
    logging: Logging,
) : Scanner {

    private val logger = Logger(logging, tag = "Kable/Scanner", identifier = null)

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        ?: error("Bluetooth not supported")

    public override val advertisements: Flow<Advertisement> = callbackFlow {
        val scanner = bluetoothAdapter.bluetoothLeScanner ?: throw BluetoothDisabledException()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySendBlocking(Advertisement(result))
                    .onFailure {
                        logger.warn { message = "Unable to deliver scan result due to failure in flow or premature closing." }
                    }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                runCatching {
                    results.forEach {
                        trySendBlocking(Advertisement(it)).getOrThrow()
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

        logger.info {
            message = if (filters?.isEmpty() != false) {
                "Starting scan without filters"
            } else {
                "Starting scan with ${filters.size} filter(s)"
            }
        }
        val scanFilters = filters?.map { filter ->
            ScanFilter.Builder().apply {
                when (filter) {
                    is DeviceInfo -> {
                        filter.address?.let { setDeviceAddress(it) }
                        filter.name?.let { setDeviceName(it) }
                    }
                    is ManufacturerData ->
                        setManufacturerData(filter.id, filter.data, filter.dataMask)
                    is Service ->
                        setServiceUuid(ParcelUuid(filter.uuid)).build()
                }
            }.build()
        }.orEmpty()
        scanner.startScan(scanFilters, scanSettings, callback)

        awaitClose {
            logger.info {
                message = if (scanFilters.isEmpty()) {
                    "Stopping scan without filters"
                } else {
                    "Stopping scan with ${filters?.size ?: 0} filter(s)"
                }
            }
            // Can't check BLE state here, only Bluetooth, but should assume `IllegalStateException` means BLE has been disabled.
            try {
                scanner.stopScan(callback)
            } catch (e: IllegalStateException) {
                logger.warn(e) { message = "Failed to stop scan. " }
            }
        }
    }.flowOn(Dispatchers.Main.immediate)
}
