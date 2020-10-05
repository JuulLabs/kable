package com.juul.kable.gatt

import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.STATE_CONNECTED
import android.bluetooth.BluetoothProfile.STATE_CONNECTING
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTING
import com.juul.kable.external.GATT_CONN_CANCEL
import com.juul.kable.external.GATT_CONN_FAIL_ESTABLISH
import com.juul.kable.external.GATT_CONN_L2C_FAILURE
import com.juul.kable.external.GATT_CONN_LMP_TIMEOUT
import com.juul.kable.external.GATT_CONN_TERMINATE_LOCAL_HOST
import com.juul.kable.external.GATT_CONN_TERMINATE_PEER_USER
import com.juul.kable.external.GATT_CONN_TIMEOUT

internal data class OnConnectionStateChange(
    val status: ConnectionStatus,
    val newState: ConnectionState,
)

internal data class OnCharacteristicChanged(
    val characteristic: BluetoothGattCharacteristic,
    val value: ByteArray,
)

/**
 * Represents the possible GATT connection statuses as defined in the Android source code.
 *
 * - [GATT_SUCCESS]
 * - [GATT_CONN_L2C_FAILURE]
 * - [GATT_CONN_L2C_FAILURE]
 * - [GATT_CONN_TIMEOUT]
 * - [GATT_CONN_TERMINATE_PEER_USER]
 * - [GATT_CONN_TERMINATE_LOCAL_HOST]
 * - [GATT_CONN_FAIL_ESTABLISH]
 * - [GATT_CONN_LMP_TIMEOUT]
 * - [GATT_CONN_CANCEL]
 */
internal inline class ConnectionStatus(private val value: Int) {
    override fun toString(): String = when (value) {
        GATT_SUCCESS -> "GATT_SUCCESS"
        GATT_CONN_L2C_FAILURE -> "GATT_CONN_L2C_FAILURE"
        GATT_CONN_TIMEOUT -> "GATT_CONN_TIMEOUT"
        GATT_CONN_TERMINATE_PEER_USER -> "GATT_CONN_TERMINATE_PEER_USER"
        GATT_CONN_TERMINATE_LOCAL_HOST -> "GATT_CONN_TERMINATE_LOCAL_HOST"
        GATT_CONN_FAIL_ESTABLISH -> "GATT_CONN_FAIL_ESTABLISH"
        GATT_CONN_LMP_TIMEOUT -> "GATT_CONN_LMP_TIMEOUT"
        GATT_CONN_CANCEL -> "GATT_CONN_CANCEL"
        else -> "GATT_CONN_UNKNOWN"
    }.let { name -> "$name($value)" }
}

/**
 * Represents the possible GATT states as defined in [BluetoothProfile]:
 *
 * - [BluetoothProfile.STATE_DISCONNECTED]
 * - [BluetoothProfile.STATE_CONNECTING]
 * - [BluetoothProfile.STATE_CONNECTED]
 * - [BluetoothProfile.STATE_DISCONNECTING]
 */
internal inline class ConnectionState(private val value: Int) {
    override fun toString(): String = when (value) {
        STATE_DISCONNECTING -> "STATE_DISCONNECTING"
        STATE_DISCONNECTED -> "STATE_DISCONNECTED"
        STATE_CONNECTING -> "STATE_CONNECTING"
        STATE_CONNECTED -> "STATE_CONNECTED"
        else -> "STATE_UNKNOWN"
    }.let { name -> "$name($value)" }
}
