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
import com.juul.kable.logs.LoggingBuilder
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

public class ScanFailedException internal constructor(
    public val errorCode: Int,
) : IllegalStateException("Bluetooth scan failed with error code $errorCode")

public actual fun Scanner(services: List<Uuid>?): Scanner = Scanner(services) { }

public fun Scanner(services: List<Uuid>?, configureLogging: LoggingBuilder): Scanner =
    AndroidScanner(services, Logging().apply(configureLogging))

public class AndroidScanner internal constructor(
    private val filterServices: List<Uuid>?,
    logging: Logging,
) : Scanner {

    private val logger = Logger(logging, tag = "Kable/Scanner")

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        ?: error("Bluetooth not supported")

    public override val advertisements: Flow<Advertisement> = callbackFlow {
        check(bluetoothAdapter.isEnabled) { "Bluetooth is disabled" }

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
                cancel("Bluetooth scan failed", ScanFailedException(errorCode))
            }
        }

        val scanFilter =
            filterServices
                ?.map { ScanFilter.Builder().setServiceUuid(ParcelUuid(it)).build() }
                ?.toList()
        bluetoothAdapter.bluetoothLeScanner.startScan(
            scanFilter,
            ScanSettings.Builder().build(),
            callback,
        )

        awaitClose {
            bluetoothAdapter.bluetoothLeScanner.stopScan(callback)
        }
    }
}
