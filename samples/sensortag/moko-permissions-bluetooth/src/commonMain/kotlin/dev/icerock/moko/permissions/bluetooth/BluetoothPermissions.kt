/*
 * Copyright 2025 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.permissions.bluetooth

import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionDelegate

internal expect val bluetoothLEDelegate: PermissionDelegate
internal expect val bluetoothScanDelegate: PermissionDelegate
internal expect val bluetoothAdvertiseDelegate: PermissionDelegate
internal expect val bluetoothConnectDelegate: PermissionDelegate

object BluetoothLEPermission : Permission {
    override val delegate get() = bluetoothLEDelegate
}

object BluetoothScanPermission : Permission {
    override val delegate get() = bluetoothScanDelegate
}

object BluetoothAdvertisePermission : Permission {
    override val delegate get() = bluetoothAdvertiseDelegate
}

object BluetoothConnectPermission : Permission {
    override val delegate get() = bluetoothConnectDelegate
}

val Permission.Companion.BLUETOOTH_LE get() = BluetoothLEPermission
val Permission.Companion.BLUETOOTH_SCAN get() = BluetoothScanPermission
val Permission.Companion.BLUETOOTH_ADVERTISE get() = BluetoothAdvertisePermission
val Permission.Companion.BLUETOOTH_CONNECT get() = BluetoothConnectPermission
