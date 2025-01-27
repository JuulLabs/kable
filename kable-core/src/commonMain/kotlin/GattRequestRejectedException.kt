package com.juul.kable

/**
 * Thrown on Android when underlying `android.bluetooth.BluetoothDevice` method call returns `false`.
 * This can occur under the following conditions:
 *
 * - Request isn't allowed (e.g. reading a non-readable characteristic)
 * - Underlying service or client interface is missing or invalid (e.g. `mService == null || mClientIf == 0`)
 * - Associated `BluetoothDevice` is unavailable
 * - Device is busy (i.e. a previous request is still in-progress)
 * - An Android internal failure occurred (i.e. an underlying `android.os.RemoteException` was thrown)
 */
public open class GattRequestRejectedException internal constructor(
    message: String? = null,
) : IllegalStateException(message)
