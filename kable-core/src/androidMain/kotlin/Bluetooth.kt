package com.juul.kable

import android.bluetooth.BluetoothAdapter.ERROR
import android.bluetooth.BluetoothAdapter.EXTRA_STATE
import android.bluetooth.BluetoothAdapter.STATE_OFF
import android.bluetooth.BluetoothAdapter.STATE_ON
import android.bluetooth.BluetoothAdapter.STATE_TURNING_OFF
import android.bluetooth.BluetoothAdapter.STATE_TURNING_ON
import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import android.location.LocationManager.EXTRA_PROVIDER_ENABLED
import android.location.LocationManager.PROVIDERS_CHANGED_ACTION
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
import com.juul.tuulbox.coroutines.flow.broadcastReceiverFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
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
        ?: throw LocationManagerUnavailableException("LocationManager system service unavailable")

private val isLocationEnabled: Boolean
    get() = LocationManagerCompat.isLocationEnabled(applicationContext.locationManager)

private val locationEnabledFlow = when {
    SDK_INT > R -> flowOf(true)
    else -> broadcastReceiverFlow(IntentFilter(PROVIDERS_CHANGED_ACTION))
        .map { intent ->
            if (SDK_INT == R) {
                intent.getBooleanExtra(EXTRA_PROVIDER_ENABLED, false)
            } else {
                isLocationEnabled
            }
        }
        .onStart { emit(isLocationEnabled) }
        .distinctUntilChanged()
}

private val bluetoothStateFlow =
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
            val isEnabled = when (getBluetoothAdapterOrNull()?.isEnabled) {
                true -> Available
                else -> Unavailable(reason = Off)
            }
            emit(isEnabled)
        }

internal actual val bluetoothAvailability: Flow<Bluetooth.Availability> =
    combine(
        locationEnabledFlow,
        bluetoothStateFlow,
    ) { locationEnabled, bluetoothState ->
        if (locationEnabled) bluetoothState else Unavailable(reason = LocationServicesDisabled)
    }
