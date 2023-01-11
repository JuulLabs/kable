package com.juul.sensortag

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Build.VERSION_CODES.R
import com.juul.kable.Bluetooth

val Bluetooth.permissionsNeeded: List<String> by lazy {
    when {
        SDK_INT > R -> listOf(BLUETOOTH_SCAN, BLUETOOTH_CONNECT)
        SDK_INT > P -> listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
        else -> listOf(ACCESS_FINE_LOCATION)
    }
}
