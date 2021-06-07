package com.juul.kable.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_CONNECTION_CONGESTED
import android.bluetooth.BluetoothGatt.GATT_FAILURE
import android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION
import android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION
import android.bluetooth.BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH
import android.bluetooth.BluetoothGatt.GATT_INVALID_OFFSET
import android.bluetooth.BluetoothGatt.GATT_READ_NOT_PERMITTED
import android.bluetooth.BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGatt.GATT_WRITE_NOT_PERMITTED
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.juul.kable.external.GATT_AUTH_FAIL
import com.juul.kable.external.GATT_BUSY
import com.juul.kable.external.GATT_CCC_CFG_ERR
import com.juul.kable.external.GATT_CMD_STARTED
import com.juul.kable.external.GATT_DB_FULL
import com.juul.kable.external.GATT_ENCRYPED_NO_MITM
import com.juul.kable.external.GATT_ERROR
import com.juul.kable.external.GATT_ERR_UNLIKELY
import com.juul.kable.external.GATT_ILLEGAL_PARAMETER
import com.juul.kable.external.GATT_INSUF_AUTHORIZATION
import com.juul.kable.external.GATT_INSUF_KEY_SIZE
import com.juul.kable.external.GATT_INSUF_RESOURCE
import com.juul.kable.external.GATT_INTERNAL_ERROR
import com.juul.kable.external.GATT_INVALID_CFG
import com.juul.kable.external.GATT_INVALID_HANDLE
import com.juul.kable.external.GATT_INVALID_PDU
import com.juul.kable.external.GATT_MORE
import com.juul.kable.external.GATT_NOT_ENCRYPTED
import com.juul.kable.external.GATT_NOT_FOUND
import com.juul.kable.external.GATT_NOT_LONG
import com.juul.kable.external.GATT_NO_RESOURCES
import com.juul.kable.external.GATT_OUT_OF_RANGE
import com.juul.kable.external.GATT_PENDING
import com.juul.kable.external.GATT_PRC_IN_PROGRESS
import com.juul.kable.external.GATT_PREPARE_Q_FULL
import com.juul.kable.external.GATT_SERVICE_STARTED
import com.juul.kable.external.GATT_UNSUPPORT_GRP_TYPE
import com.juul.kable.external.GATT_WRONG_STATE

internal sealed class Response {

    abstract val status: GattStatus

    data class OnReadRemoteRssi(
        val rssi: Int,
        override val status: GattStatus,
    ) : Response()

    data class OnMtuChanged(
        val mtu: Int,
        override val status: GattStatus,
    ) : Response()

    data class OnServicesDiscovered(
        override val status: GattStatus,
    ) : Response()

    data class OnCharacteristicRead(
        val characteristic: BluetoothGattCharacteristic,
        val value: ByteArray?,
        override val status: GattStatus,
    ) : Response() {
        override fun toString(): String =
            "OnCharacteristicRead(characteristic=${characteristic.uuid}, value=${value?.size ?: 0} bytes, status=$status)"
    }

    data class OnCharacteristicWrite(
        val characteristic: BluetoothGattCharacteristic,
        override val status: GattStatus,
    ) : Response() {
        override fun toString(): String =
            "OnCharacteristicWrite(characteristic=${characteristic.uuid}, status=$status)"
    }

    data class OnDescriptorRead(
        val descriptor: BluetoothGattDescriptor,
        val value: ByteArray?,
        override val status: GattStatus,
    ) : Response() {
        override fun toString(): String =
            "OnDescriptorRead(descriptor=${descriptor.uuid}, value=${value?.size ?: 0} bytes, status=$status)"
    }

    data class OnDescriptorWrite(
        val descriptor: BluetoothGattDescriptor,
        override val status: GattStatus,
    ) : Response() {
        override fun toString(): String =
            "OnDescriptorWrite(descriptor=${descriptor.uuid}, status=$status)"
    }
}

/**
 * Represents the possible GATT statuses as defined in [BluetoothGatt]:
 *
 * - [BluetoothGatt.GATT_SUCCESS]
 * - [BluetoothGatt.GATT_READ_NOT_PERMITTED]
 * - [BluetoothGatt.GATT_WRITE_NOT_PERMITTED]
 * - [BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION]
 * - [BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED]
 * - [BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION]
 * - [BluetoothGatt.GATT_INVALID_OFFSET]
 * - [BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH]
 * - [BluetoothGatt.GATT_CONNECTION_CONGESTED]
 * - [BluetoothGatt.GATT_FAILURE]
 */
internal inline class GattStatus(private val value: Int) {
    override fun toString(): String = when (value) {
        GATT_SUCCESS -> "GATT_SUCCESS"
        GATT_INVALID_HANDLE -> "GATT_INVALID_HANDLE"
        GATT_READ_NOT_PERMITTED -> "GATT_READ_NOT_PERMITTED"
        GATT_WRITE_NOT_PERMITTED -> "GATT_WRITE_NOT_PERMITTED"
        GATT_INVALID_PDU -> "GATT_INVALID_PDU"
        GATT_INSUFFICIENT_AUTHENTICATION -> "GATT_INSUFFICIENT_AUTHENTICATION"
        GATT_REQUEST_NOT_SUPPORTED -> "GATT_REQUEST_NOT_SUPPORTED"
        GATT_INVALID_OFFSET -> "GATT_INVALID_OFFSET"
        GATT_INSUF_AUTHORIZATION -> "GATT_INSUF_AUTHORIZATION"
        GATT_PREPARE_Q_FULL -> "GATT_PREPARE_Q_FULL"
        GATT_NOT_FOUND -> "GATT_NOT_FOUND"
        GATT_NOT_LONG -> "GATT_NOT_LONG"
        GATT_INSUF_KEY_SIZE -> "GATT_INSUF_KEY_SIZE"
        GATT_INVALID_ATTRIBUTE_LENGTH -> "GATT_INVALID_ATTRIBUTE"
        GATT_ERR_UNLIKELY -> "GATT_ERR_UNLIKELY"
        GATT_INSUFFICIENT_ENCRYPTION -> "GATT_INSUFFICIENT_ENCRYPTION"
        GATT_UNSUPPORT_GRP_TYPE -> "GATT_UNSUPPORT_GRP_TYPE"
        GATT_INSUF_RESOURCE -> "GATT_INSUF_RESOURCE"
        GATT_ILLEGAL_PARAMETER -> "GATT_ILLEGAL_PARAMETER"
        GATT_NO_RESOURCES -> "GATT_NO_RESOURCES"
        GATT_INTERNAL_ERROR -> "GATT_INTERNAL_ERROR"
        GATT_WRONG_STATE -> "GATT_WRONG_STATE"
        GATT_DB_FULL -> "GATT_DB_FULL"
        GATT_BUSY -> "GATT_BUSY"
        GATT_ERROR -> "GATT_ERROR"
        GATT_CMD_STARTED -> "GATT_CMD_STARTED"
        GATT_PENDING -> "GATT_PENDING"
        GATT_AUTH_FAIL -> "GATT_AUTH_FAIL"
        GATT_MORE -> "GATT_MORE"
        GATT_INVALID_CFG -> "GATT_INVALID_CFG"
        GATT_SERVICE_STARTED -> "GATT_SERVICE_STARTED"
        GATT_ENCRYPED_NO_MITM -> "GATT_ENCRYPED_NO_MITM"
        GATT_NOT_ENCRYPTED -> "GATT_NOT_ENCRYPTED"
        GATT_CONNECTION_CONGESTED -> "GATT_CONNECTION_CONGESTED"
        GATT_CCC_CFG_ERR -> "GATT_CCC_CFG_ERR"
        GATT_PRC_IN_PROGRESS -> "GATT_PRC_IN_PROGRESS"
        GATT_OUT_OF_RANGE -> "GATT_OUT_OF_RANGE"
        GATT_FAILURE -> "GATT_FAILURE"
        else -> "GATT_UNKNOWN"
    }.let { name -> "$name($value)" }
}
