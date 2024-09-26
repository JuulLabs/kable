package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.os.RemoteException
import com.juul.kable.AndroidPeripheral.WriteResult

/**
 * Thrown when underlying [BluetoothGatt] method call returns `false`. This can occur under the
 * following conditions:
 *
 * - Request isn't allowed (e.g. reading a non-readable characteristic)
 * - Underlying service or client interface is missing or invalid (e.g. `mService == null || mClientIf == 0`)
 * - Associated [BluetoothDevice] is unavailable
 * - Device is busy (i.e. a previous request is still in-progress)
 * - An Android internal failure occurred (i.e. an underlying [RemoteException] was thrown)
 */
public open class GattRequestRejectedException internal constructor(
    message: String? = null,
) : IllegalStateException(message)

/**
 * Thrown when underlying [BluetoothGatt] write operation call fails.
 *
 * The reason for the failure is available via the [result] property on Android 13 (API 33) and
 * newer.
 *
 * On Android prior to API 33, [result] is always [Unknown][WriteResult.Unknown], but the failure
 * may have been due to any of the conditions listed for [GattRequestRejectedException].
 */
public class GattWriteException internal constructor(
    public val result: WriteResult,
) : GattRequestRejectedException("Write failed: $result")
