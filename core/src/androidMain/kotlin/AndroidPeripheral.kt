package com.juul.kable

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.StateFlow

@Deprecated(
    message = "Moved as nested class of `AndroidPeripheral`.",
    replaceWith = ReplaceWith("AndroidPeripheral.Priority"),
)
public typealias Priority = AndroidPeripheral.Priority

public interface AndroidPeripheral : Peripheral {

    public enum class Priority { Low, Balanced, High }

    public enum class Type {

        /** https://developer.android.com/reference/android/bluetooth/BluetoothDevice#DEVICE_TYPE_CLASSIC */
        Classic,

        /**
         * Low Energy - LE-only
         * https://developer.android.com/reference/android/bluetooth/BluetoothDevice#DEVICE_TYPE_LE
         */
        LowEnergy,

        /**
         * Dual Mode - BR/EDR/LE
         * https://developer.android.com/reference/android/bluetooth/BluetoothDevice#DEVICE_TYPE_DUAL
         */
        DualMode,

        /** https://developer.android.com/reference/android/bluetooth/BluetoothDevice#DEVICE_TYPE_UNKNOWN */
        Unknown,
    }

    /**
     * Get the type of the peripheral.
     *
     * Per [Making Android BLE work — part 1: Clearing the cache](https://medium.com/@martijn.van.welie/making-android-ble-work-part-1-a736dcd53b02#42d8),
     * when [type] is [Unknown][Type.Unknown], a scan should be performed before [connect].
     *
     * For apps targeting [R][Build.VERSION_CODES.R] or lower, [type] requires the
     * [BLUETOOTH][Manifest.permission.BLUETOOTH] permission which can be gained with a simple
     * `<uses-permission>` manifest tag.
     *
     * For apps targeting [S][Build.VERSION_CODES.S] or or higher, [type] requires the
     * [BLUETOOTH_CONNECT][Manifest.permission.BLUETOOTH_CONNECT] permission which can be gained
     * with `Activity.requestPermissions(String[], int)`.
     */
    @get:RequiresPermission(
        anyOf = ["android.permission.BLUETOOTH", "android.permission.BLUETOOTH_CONNECT"],
    )
    public val type: Type

    /**
     * Returns the hardware address of this [AndroidPeripheral].
     *
     * For example, "00:11:22:AA:BB:CC".
     */
    public val address: String

    public fun requestConnectionPriority(priority: Priority): Boolean

    /**
     * Requests that the current connection's MTU be changed. Suspends until the MTU changes, or failure occurs. The
     * negotiated MTU value is returned, which may not be [mtu] value requested if the remote peripheral negotiated an
     * alternate MTU.
     *
     * @throws NotReadyException if invoked without an established [connection][Peripheral.connect].
     * @throws GattRequestRejectedException if Android was unable to fulfill the MTU change request.
     * @throws GattStatusException if MTU change request failed.
     */
    public suspend fun requestMtu(mtu: Int): Int

    /**
     * [StateFlow] of the most recently negotiated MTU. The MTU will change upon a successful request to change the MTU
     * (via [requestMtu]), or if the peripheral initiates an MTU change. [StateFlow]'s `value` will be `null` until MTU
     * is negotiated.
     */
    public val mtu: StateFlow<Int?>
}
