package dev.icerock.moko.permissions.bluetooth

import dev.icerock.moko.permissions.PermissionDelegate

internal actual val bluetoothLEDelegate: PermissionDelegate = object : PermissionDelegate {}
internal actual val bluetoothScanDelegate: PermissionDelegate = object : PermissionDelegate {}
internal actual val bluetoothAdvertiseDelegate: PermissionDelegate = object : PermissionDelegate {}
internal actual val bluetoothConnectDelegate: PermissionDelegate = object : PermissionDelegate {}
