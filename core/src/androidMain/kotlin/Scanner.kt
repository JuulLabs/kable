package com.juul.kable

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import com.benasher44.uuid.Uuid
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
import kotlinx.coroutines.withContext

public class ScanFailedException internal constructor(
    public val errorCode: Int,
) : IllegalStateException("Bluetooth scan failed with error code $errorCode")

public class AndroidScanner internal constructor(
    private val filterServices: List<Uuid>?,
    private val scanSettings: ScanSettings,
    logging: Logging,
) : Scanner {

    private val logger = Logger(logging, tag = "Kable/Scanner", identifier = null)

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        ?: error("Bluetooth not supported")

    public override val advertisements: Flow<Advertisement> = callbackFlow {
        val scanner = checkNotNull(bluetoothAdapter.bluetoothLeScanner) { "Bluetooth disabled." }

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

        val scanFilter = filterServices
            ?.map { ScanFilter.Builder().setServiceUuid(ParcelUuid(it)).build() }
            ?.toList()
        logger.info {
            message = when (filterServices) {
                null -> "Starting scan with no service filter."
                else -> "Starting scan for services ${filterServices.joinToString()}."
            }
        }
        scanner.startScan(scanFilter, scanSettings, callback)

        awaitClose {
            logger.info {
                message = when (filterServices) {
                    null -> "Stopping scan with no service filter."
                    else -> "Stopping scan for services ${filterServices.joinToString()}."
                }
            }
            try {
                scanner.stopScan(callback)
            } catch (e: IllegalStateException) {
                logger.warn { message = "Failed to stop scan." }
            }
        }
    }.flowOn(Dispatchers.Main.immediate)
}
