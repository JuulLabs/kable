package com.juul.kable.external

// https://android.googlesource.com/platform/external/libnfc-nci/+/lollipop-release/src/include/hcidefs.h#447
private const val HCI_ERR_CONNECTION_TOUT = 0x08
private const val HCI_ERR_PEER_USER = 0x13
private const val HCI_ERR_CONN_CAUSE_LOCAL_HOST = 0x16
private const val HCI_ERR_LMP_RESPONSE_TIMEOUT = 0x22
private const val HCI_ERR_CONN_FAILED_ESTABLISHMENT = 0x3E

// https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/lollipop-release/stack/include/l2cdefs.h#87
private const val L2CAP_CONN_CANCEL = 256

// https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/lollipop-release/stack/include/gatt_api.h#106
internal const val GATT_CONN_L2C_FAILURE = 1
internal const val GATT_CONN_TIMEOUT = HCI_ERR_CONNECTION_TOUT
internal const val GATT_CONN_TERMINATE_PEER_USER = HCI_ERR_PEER_USER
internal const val GATT_CONN_TERMINATE_LOCAL_HOST = HCI_ERR_CONN_CAUSE_LOCAL_HOST
internal const val GATT_CONN_FAIL_ESTABLISH = HCI_ERR_CONN_FAILED_ESTABLISHMENT
internal const val GATT_CONN_LMP_TIMEOUT = HCI_ERR_LMP_RESPONSE_TIMEOUT
internal const val GATT_CONN_CANCEL = L2CAP_CONN_CANCEL

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

// https://android.googlesource.com/platform/development/+/7167a054a8027f75025c865322fa84791a9b3bd1/samples/BluetoothLeGatt/src/com/example/bluetooth/le/SampleGattAttributes.java#27
internal const val CLIENT_CHARACTERISTIC_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb"
