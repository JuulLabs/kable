package com.juul.kable

import kotlinx.coroutines.flow.Flow


enum class BluetoothState {
    poweredOn,
    poweredOff,
    resetting,
    unsupported,
    unknown,
    unauthorized,
    locationServiceDisabled //Only used on Android for versions 11 and lower
}

public expect val bluetoothStatusFlow: Flow<BluetoothState>