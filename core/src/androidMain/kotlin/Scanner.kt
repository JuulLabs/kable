package com.juul.kable

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

public class ScanFailedException internal constructor(
    public val errorCode: Int
) : IllegalStateException("Bluetooth scan failed with error code $errorCode")

public actual fun Scanner(): Scanner = AndroidScanner()

public class AndroidScanner internal constructor() : Scanner {

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        ?: error("Bluetooth not supported")

    public override val advertisements: Flow<Advertisement> = callbackFlow {
        check(bluetoothAdapter.isEnabled) { "Bluetooth is disabled" }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                runCatching {
                    sendBlocking(Advertisement(result))
                }.onFailure { cause -> Log.w(TAG, "Unable to deliver scan result.", cause) }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                runCatching {
                    results.forEach {
                        sendBlocking(Advertisement(it))
                    }
                }.onFailure { cause -> Log.w(TAG, "Unable to deliver batch scan results.", cause) }
            }

            override fun onScanFailed(errorCode: Int) {
                cancel("Bluetooth scan failed", ScanFailedException(errorCode))
            }
        }
        bluetoothAdapter.bluetoothLeScanner.startScan(callback)
        awaitClose {
            bluetoothAdapter.bluetoothLeScanner.stopScan(callback)
        }
    }
}
