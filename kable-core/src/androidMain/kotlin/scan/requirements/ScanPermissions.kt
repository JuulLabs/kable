package com.juul.kable.scan.requirements

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH_SCAN
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Build.VERSION_CODES.R
import androidx.core.content.ContextCompat
import com.juul.kable.applicationContext

private val requiredPermission = when {
    // If your app targets Android 9 (API level 28) or lower, you can declare the ACCESS_COARSE_LOCATION permission
    // instead of the ACCESS_FINE_LOCATION permission.
    // https://developer.android.com/guide/topics/connectivity/bluetooth/permissions#declare-android11-or-lower
    SDK_INT <= P -> ACCESS_COARSE_LOCATION

    // ACCESS_FINE_LOCATION is necessary because, on Android 11 (API level 30) and lower, a Bluetooth scan could
    // potentially be used to gather information about the location of the user.
    // https://developer.android.com/guide/topics/connectivity/bluetooth/permissions#declare-android11-or-lower
    SDK_INT <= R -> ACCESS_FINE_LOCATION

    // If your app targets Android 12 (API level 31) or higher, declare the following permissions in your app's
    // manifest file:
    //
    // 1. If your app looks for Bluetooth devices, such as BLE peripherals, declare the `BLUETOOTH_SCAN` permission.
    // 2. If your app makes the current device discoverable to other Bluetooth devices, declare the
    //    `BLUETOOTH_ADVERTISE` permission.
    // 3. If your app communicates with already-paired Bluetooth devices, declare the BLUETOOTH_CONNECT permission.
    // https://developer.android.com/guide/topics/connectivity/bluetooth/permissions#declare-android12-or-higher
    else /* SDK_INT >= S */ -> BLUETOOTH_SCAN
}

/** @throws IllegalStateException If the required permissions for scanning have not been granted. */
internal fun checkScanPermissions() {
    if (ContextCompat.checkSelfPermission(applicationContext, requiredPermission) != PERMISSION_GRANTED) {
        error("Missing required $requiredPermission for scanning")
    }
}
