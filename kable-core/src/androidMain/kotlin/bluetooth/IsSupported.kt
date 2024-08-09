package com.juul.kable.bluetooth

import com.juul.kable.getBluetoothAdapterOrNull

internal actual suspend fun isSupported(): Boolean =
    getBluetoothAdapterOrNull() != null
