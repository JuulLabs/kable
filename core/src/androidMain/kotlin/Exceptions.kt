package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.os.RemoteException

public actual typealias IOException = java.io.IOException

/**
 * Thrown when underlying [BluetoothGatt] method call returns `false`. This can occur under the following conditions:
 *
 * - Request isn't allowed (e.g. reading a non-readable characteristic)
 * - Underlying service or client interface is missing or invalid (e.g. `mService == null || mClientIf == 0`)
 * - Associated [BluetoothDevice] is unavailable
 * - Device is busy (i.e. a previous request is still in-progress)
 * - An Android internal failure occurred (i.e. an underlying [RemoteException] was thrown)
 */
public class GattRequestRejectedException internal constructor(
    message: String? = null,
    cause: Throwable? = null,
) : BluetoothLeException(message, cause)
