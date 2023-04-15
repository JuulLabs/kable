package com.juul.kable

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ERROR
import android.bluetooth.BluetoothAdapter.EXTRA_STATE
import android.bluetooth.BluetoothAdapter.STATE_OFF
import android.bluetooth.BluetoothAdapter.STATE_ON
import android.bluetooth.BluetoothAdapter.STATE_TURNING_OFF
import android.bluetooth.BluetoothAdapter.STATE_TURNING_ON
import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.R
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.juul.kable.Bluetooth.Availability.Available
import com.juul.kable.Bluetooth.Availability.Unavailable
import com.juul.kable.Reason.LocationServicesDisabled
import com.juul.kable.Reason.Off
import com.juul.kable.Reason.TurningOff
import com.juul.kable.Reason.TurningOn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED as BLUETOOTH_STATE_CHANGED

public actual enum class Reason {
    Off, // BluetoothAdapter.STATE_OFF
    TurningOff, // BluetoothAdapter.STATE_TURNING_OFF or BluetoothAdapter.STATE_BLE_TURNING_OFF
    TurningOn, // BluetoothAdapter.STATE_TURNING_ON or BluetoothAdapter.STATE_BLE_TURNING_ON

    /** Only applicable on Android 11 (API 30) and lower. */
    LocationServicesDisabled,
}

private val Context.locationManager: LocationManager
    get() = ContextCompat.getSystemService(this, LocationManager::class.java)
        ?: error("LocationManager system service unavailable")

private fun isLocationEnabled(): Boolean {
    return LocationManagerCompat.isLocationEnabled(applicationContext.locationManager)
}

private val locationNotNeededStateFlow: Flow<Boolean> =
    when {
        SDK_INT > R -> MutableStateFlow(true)
        else -> broadcastReceiverFlow(IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
            .map { intent ->
                intent.getBooleanExtra(LocationManager.EXTRA_PROVIDER_ENABLED, false)
            }
            .onStart { emit(isLocationEnabled() || SDK_INT > R) }.distinctUntilChanged()
    }

private val bluetoothStateChangeReceiverFlow =
    broadcastReceiverFlow(IntentFilter(BLUETOOTH_STATE_CHANGED))
        .map { intent -> intent.getIntExtra(EXTRA_STATE, ERROR) }
        .map { state ->
            when (state) {
                STATE_ON -> Available
                STATE_OFF -> Unavailable(reason = Off)
                STATE_TURNING_OFF -> Unavailable(reason = TurningOff)
                STATE_TURNING_ON -> Unavailable(reason = TurningOn)
                else -> error("Unexpected bluetooth state: $state")
            }
        }
        .onStart {
            val availability = if (SDK_INT <= R && !isLocationEnabled()) {
                Unavailable(reason = LocationServicesDisabled)
            } else {
                when (BluetoothAdapter.getDefaultAdapter()?.isEnabled) {
                    true -> Available
                    else -> Unavailable(reason = Off)
                }
            }
            emit(availability)
        }

internal actual val bluetoothAvailability: Flow<Bluetooth.Availability> =
    combine(
        bluetoothStateChangeReceiverFlow,
        locationNotNeededStateFlow,
    ) { bluetooth, locationNotNeededStateFlow ->
        if (locationNotNeededStateFlow) bluetooth else Unavailable(reason = LocationServicesDisabled)
    }
