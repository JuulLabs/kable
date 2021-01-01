package com.juul.kable

import android.bluetooth.BluetoothGatt

/** Mode specifying if config descriptor (0x2902) should be written to when starting/stopping an observation. */
public enum class WriteNotificationDescriptor {

    /**
     * If config descriptor exists for characteristic being observed, then it will be written to when starting/stopping
     * observations. If it does not exist, then automatically fallback to only enabling/disabling notifications (via
     * [BluetoothGatt.setCharacteristicNotification]).
     */
    Auto,

    /**
     * Always write to config descriptor for characteristic being observed. If it does not exist then an exception is
     * thrown when starting/stopping the observation.
     */
    Always,

    /** Never write to config descriptor for characteristic being observed, regardless of its availability. */
    Never,
}
