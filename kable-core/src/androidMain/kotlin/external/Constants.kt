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
