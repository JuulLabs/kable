package com.juul.kable

import android.Manifest
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothStatusCodes
import android.os.Build
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.StateFlow

@Deprecated(
    message = "Moved as nested class of `AndroidPeripheral`.",
    replaceWith = ReplaceWith("AndroidPeripheral.Priority"),
    level = DeprecationLevel.HIDDEN,
)
public typealias Priority = AndroidPeripheral.Priority

public interface AndroidPeripheral : Peripheral {

    public enum class Priority { Low, Balanced, High }

    /** https://developer.android.com/reference/android/bluetooth/BluetoothDevice#getBondState() */
    public enum class Bond { None, Bonding, Bonded }

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
     * Represents possible write operation results, as defined by Android's
     * [WriteOperationReturnValues](https://cs.android.com/android/platform/superproject/main/+/b7a389a145ff443550e1a942bf713c60c2bd6a14:packages/modules/Bluetooth/framework/java/android/bluetooth/BluetoothGatt.java;l=1587-1593)
     * `IntDef`.
     */
    public enum class WriteResult {

        /**
         * Error code indicating that the Bluetooth Device specified is not connected, but is bonded.
         *
         * https://cs.android.com/android/platform/superproject/main/+/main:packages/modules/Bluetooth/framework/java/android/bluetooth/BluetoothStatusCodes.java;l=50
         */
        NotConnected,

        /**
         * A GATT writeCharacteristic request is not permitted on the remote device.
         *
         * See: [BluetoothStatusCodes.ERROR_GATT_WRITE_NOT_ALLOWED]
         * https://developer.android.com/reference/kotlin/android/bluetooth/BluetoothStatusCodes#error_gatt_write_not_allowed
         */
        WriteNotAllowed,

        /**
         * A GATT writeCharacteristic request is issued to a busy remote device.
         *
         * See: [BluetoothStatusCodes.ERROR_GATT_WRITE_REQUEST_BUSY]
         * https://developer.android.com/reference/kotlin/android/bluetooth/BluetoothStatusCodes#error_gatt_write_request_busy
         */
        WriteRequestBusy,

        /**
         * Error code indicating that the caller does not have the [BLUETOOTH_CONNECT] permission.
         *
         * See: [BluetoothStatusCodes.ERROR_MISSING_BLUETOOTH_CONNECT_PERMISSION]
         * https://developer.android.com/reference/kotlin/android/bluetooth/BluetoothStatusCodes#error_missing_bluetooth_connect_permission
         */
        MissingBluetoothConnectPermission,

        /**
         * Error code indicating that the profile service is not bound. You can bind a profile service
         * by calling [BluetoothAdapter.getProfileProxy].
         *
         * See: [BluetoothStatusCodes.ERROR_PROFILE_SERVICE_NOT_BOUND]
         * https://developer.android.com/reference/kotlin/android/bluetooth/BluetoothStatusCodes#error_profile_service_not_bound
         */
        ProfileServiceNotBound,

        /**
         * Indicates that an unknown error has occurred.
         *
         * See: [BluetoothStatusCodes.ERROR_UNKNOWN]
         * https://developer.android.com/reference/kotlin/android/bluetooth/BluetoothStatusCodes#error_unknown
         */
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
     * @throws NotConnectedException if invoked without an established [connection][Peripheral.connect].
     * @throws GattRequestRejectedException if Android was unable to fulfill the MTU change request.
     * @throws GattStatusException if MTU change request failed.
     */
    public suspend fun requestMtu(mtu: Int): Int

    /**
     * @see Peripheral.write
     * @throws NotConnectedException if invoked without an established [connection][connect].
     * @throws GattWriteException if underlying [BluetoothGatt] write operation call fails.
     */
    override suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    )

    /**
     * @see Peripheral.write
     * @throws NotConnectedException if invoked without an established [connection][connect].
     * @throws GattWriteException if underlying [BluetoothGatt] write operation call fails.
     */
    override suspend fun write(descriptor: Descriptor, data: ByteArray)

    /**
     * [StateFlow] of the most recently negotiated MTU. The MTU will change upon a successful request to change the MTU
     * (via [requestMtu]), or if the peripheral initiates an MTU change. [StateFlow]'s `value` will be `null` until MTU
     * is negotiated.
     */
    public val mtu: StateFlow<Int?>

    /**
     * [StateFlow] of this peripheral's bond state, updated from `ACTION_BOND_STATE_CHANGED` broadcasts. Bonding is
     * independent of the connection, so this is available whether or not the peripheral is connected.
     *
     * The broadcast receiver is registered on first access and lives until the peripheral is [closed][close].
     */
    @ExperimentalKableApi
    @get:RequiresPermission(
        anyOf = ["android.permission.BLUETOOTH", "android.permission.BLUETOOTH_CONNECT"],
    )
    public val bondState: StateFlow<Bond>

    /**
     * Bonds with this peripheral, suspending until bonding settles. Returns [Bond.Bonded] on success (immediately, if
     * already bonded) or [Bond.None] if bonding failed or was rejected. Joins a bond already in progress rather than
     * starting another.
     *
     * Does not time out: a pairing prompt waits on the user. Wrap in `withTimeout` to bound it.
     */
    @ExperimentalKableApi
    @RequiresPermission(
        anyOf = ["android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH_CONNECT"],
    )
    public suspend fun bond(): Bond

    /**
     * Removes the bond with this peripheral, as "Forget" in system settings does. Returns `true` if removal was
     * started; observe [bondState] for completion.
     *
     * Invokes the hidden `BluetoothDevice.removeBond()` via reflection, so may stop working on a future Android
     * release, in which case `false` is returned.
     */
    @ExperimentalKableApi
    @RequiresPermission(
        anyOf = ["android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH_CONNECT"],
    )
    public fun removeBond(): Boolean

    /**
     * This is an internal API and may be removed from a future release. If you are using it, please
     * open an issue and report your use case.
     */
    @InternalKableApi
    public val bluetoothDevice: BluetoothDevice
}
