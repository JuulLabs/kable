package com.juul.kable.bluetooth

import android.content.pm.PackageManager.FEATURE_BLUETOOTH_LE
import com.juul.kable.applicationContext
import com.juul.kable.getBluetoothAdapterOrNull

internal actual suspend fun isSupported(): Boolean =
    applicationContext.packageManager.hasSystemFeature(FEATURE_BLUETOOTH_LE) &&
        getBluetoothAdapterOrNull() != null
