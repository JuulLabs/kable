package com.juul.kable.android

import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import android.location.LocationManager.EXTRA_PROVIDER_ENABLED
import android.location.LocationManager.PROVIDERS_CHANGED_ACTION
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.R
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.juul.kable.applicationContext
import com.juul.tuulbox.coroutines.flow.broadcastReceiverFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal val locationEnabledOrNull = when {
    SDK_INT > R -> flowOf(true)
    else -> broadcastReceiverFlow(IntentFilter(PROVIDERS_CHANGED_ACTION))
        .map { intent ->
            if (SDK_INT == R) {
                intent.getBooleanExtra(EXTRA_PROVIDER_ENABLED, false)
            } else {
                applicationContext.isLocationEnabledOrNull()
            }
        }
        .onStart { emit(applicationContext.isLocationEnabledOrNull()) }
        .distinctUntilChanged()
        // TODO: shareIn?
}

private fun Context.isLocationEnabledOrNull(): Boolean? =
    getLocationManagerOrNull()?.let(LocationManagerCompat::isLocationEnabled)

private fun Context.getLocationManagerOrNull() =
    ContextCompat.getSystemService(this, LocationManager::class.java)
