package com.juul.kable

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine

private val bluetoothStatus = callbackFlow<BluetoothState> {
    trySendBlocking(getBluetoothStatus())
        .onFailure { error("") }
    val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> {
                        trySendBlocking(BluetoothState.poweredOff)
                            .onFailure { error("") }
                    }
                    BluetoothAdapter.STATE_ON -> {
                        trySendBlocking(BluetoothState.poweredOn)
                            .onFailure { error("") }
                    }
                    else -> BluetoothState.unknown
                }
            }
        }
    }
    applicationContext.registerReceiver(receiver, filter)

    awaitClose {
        applicationContext.unregisterReceiver(receiver)
    }
}

//Location services required on android devices 11 and below
private val locationServiceStatusFlow = callbackFlow<Boolean> {
    trySendBlocking(getLocationStatus())
        .onFailure { error("") }
    val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                val manager =
                    context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                    trySendBlocking(false)
                }
                else{
                    trySendBlocking(true)
                }
            }
        }
    }
    applicationContext.registerReceiver(receiver, filter)

    awaitClose {
        applicationContext.unregisterReceiver(receiver)
    }
}

public actual val bluetoothStatusFlow: Flow<BluetoothState> =
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
        combine(bluetoothStatus, locationServiceStatusFlow) { bluetoothstatus, locationstatus ->
            if (bluetoothstatus == BluetoothState.poweredOff) BluetoothState.poweredOff
            else if ((bluetoothstatus == BluetoothState.poweredOn) && locationstatus) BluetoothState.poweredOn
            else if (!locationstatus) BluetoothState.locationServiceDisabled
            else BluetoothState.unknown
        }
    else bluetoothStatus


//Helper function to get initial state of bluetooth
private fun getBluetoothStatus(): BluetoothState {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    if (bluetoothAdapter == null) return BluetoothState.unsupported
    return if (bluetoothAdapter.isEnabled) BluetoothState.poweredOn else BluetoothState.poweredOff
}

private fun getLocationStatus(): Boolean {
    val manager =
        applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
}
