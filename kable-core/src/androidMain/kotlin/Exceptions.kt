package com.juul.kable

import android.bluetooth.BluetoothGatt
import com.juul.kable.AndroidPeripheral.WriteResult

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
