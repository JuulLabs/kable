package com.juul.kable

import android.bluetooth.BluetoothManager

@Deprecated(
    message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
        "Will be removed in a future release. " +
        "See https://github.com/JuulLabs/kable/issues/737 for more details.",
    level = DeprecationLevel.ERROR,
)
public actual enum class Reason {
    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
        level = DeprecationLevel.ERROR,
    )
    Off, // BluetoothAdapter.STATE_OFF

    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
        level = DeprecationLevel.ERROR,
    )
    TurningOff, // BluetoothAdapter.STATE_TURNING_OFF or BluetoothAdapter.STATE_BLE_TURNING_OFF

    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
        level = DeprecationLevel.ERROR,
    )
    TurningOn, // BluetoothAdapter.STATE_TURNING_ON or BluetoothAdapter.STATE_BLE_TURNING_ON

    /**
     * [BluetoothManager] unavailable or [BluetoothManager.getAdapter] returned `null` (indicating
     * that Bluetooth is not available).
     */
    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
        level = DeprecationLevel.ERROR,
    )
    AdapterNotAvailable,

    /** Only applicable on Android 11 (API 30) and lower. */
    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
        level = DeprecationLevel.ERROR,
    )
    LocationServicesDisabled,
}
