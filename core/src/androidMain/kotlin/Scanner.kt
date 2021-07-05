package com.juul.kable

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import android.util.Log
import com.benasher44.uuid.Uuid
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

public class ScanFailedException internal constructor(
    public val errorCode: Int,
) : IllegalStateException("Bluetooth scan failed with error code $errorCode")

public actual fun Scanner(services: List<Uuid>?): Scanner = AndroidScanner(services)

public class AndroidScanner internal constructor(private val filterServices: List<Uuid>?) : Scanner {

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        ?: error("Bluetooth not supported")

    public override val advertisements: Flow<Advertisement> = callbackFlow {
        check(bluetoothAdapter.isEnabled) { "Bluetooth is disabled" }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                runCatching {
                    sendBlocking(Advertisement(result))
                }.onFailure {
                    Log.w(TAG, "Unable to deliver scan result due to failure in flow or premature closing.")
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                runCatching {
                    results.forEach {
                        sendBlocking(Advertisement(it))
                    }
                }.onFailure {
                    Log.w(TAG, "Unable to deliver batch scan results due to failure in flow or premature closing.")
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
