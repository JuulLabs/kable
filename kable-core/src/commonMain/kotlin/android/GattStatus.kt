package com.juul.kable.android

import kotlin.jvm.JvmInline

// Constants on `android.bluetooth.BluetoothGatt`
internal const val GATT_SUCCESS = 0
internal const val GATT_READ_NOT_PERMITTED = 0x2
internal const val GATT_WRITE_NOT_PERMITTED = 0x3
internal const val GATT_INSUFFICIENT_AUTHENTICATION = 0x5
internal const val GATT_REQUEST_NOT_SUPPORTED = 0x6
internal const val GATT_INVALID_OFFSET = 0x7
internal const val GATT_CONNECTION_CONGESTED = 0x8f
internal const val GATT_INVALID_ATTRIBUTE_LENGTH = 0xd
internal const val GATT_INSUFFICIENT_ENCRYPTION = 0xf
internal const val GATT_FAILURE = 0x101

// 0xE0 ~ 0xFC reserved for future use
// https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/lollipop-release/stack/include/gatt_api.h#27
internal const val GATT_INVALID_HANDLE = 0x01
internal const val GATT_INVALID_PDU = 0x04
internal const val GATT_INSUF_AUTHORIZATION = 0x08
internal const val GATT_PREPARE_Q_FULL = 0x09
internal const val GATT_NOT_FOUND = 0x0a
internal const val GATT_NOT_LONG = 0x0b
internal const val GATT_INSUF_KEY_SIZE = 0x0c
internal const val GATT_ERR_UNLIKELY = 0x0e
internal const val GATT_UNSUPPORT_GRP_TYPE = 0x10
internal const val GATT_INSUF_RESOURCE = 0x11
internal const val GATT_ILLEGAL_PARAMETER = 0x87
internal const val GATT_NO_RESOURCES = 0x80
internal const val GATT_INTERNAL_ERROR = 0x81
internal const val GATT_WRONG_STATE = 0x82
internal const val GATT_DB_FULL = 0x83
internal const val GATT_BUSY = 0x84
internal const val GATT_ERROR = 0x85
internal const val GATT_CMD_STARTED = 0x86
internal const val GATT_PENDING = 0x88
internal const val GATT_AUTH_FAIL = 0x89
internal const val GATT_MORE = 0x8a
internal const val GATT_INVALID_CFG = 0x8b
internal const val GATT_SERVICE_STARTED = 0x8c
internal const val GATT_ENCRYPED_NO_MITM = 0x8d
internal const val GATT_NOT_ENCRYPTED = 0x8e
internal const val GATT_CCC_CFG_ERR = 0xFD
internal const val GATT_PRC_IN_PROGRESS = 0xFE
internal const val GATT_OUT_OF_RANGE = 0xFF

/** Represents the possible Android GATT statuses. */
@JvmInline
internal value class GattStatus(internal val value: Int) {
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
        GATT_INVALID_ATTRIBUTE_LENGTH -> "GATT_INVALID_ATTRIBUTE_LENGTH"
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
