package com.juul.kable.scan.requirements

import android.content.Context
import android.location.LocationManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.R
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.juul.kable.UnmetRequirementException
import com.juul.kable.UnmetRequirementReason.LocationServicesDisabled
import com.juul.kable.applicationContext

internal fun checkLocationServicesEnabled() {
    if (SDK_INT > R) return
    val locationManager = applicationContext.getLocationManagerOrNull()
        ?: error("Location manager unavailable")
    if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
        throw UnmetRequirementException(
            LocationServicesDisabled,
            "Location services are required for scanning but are disabled",
        )
    }
}

private fun Context.getLocationManagerOrNull() =
    ContextCompat.getSystemService(this, LocationManager::class.java)
