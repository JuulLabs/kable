@file:JvmName("AndroidLogMessage")

package com.juul.kable.logs

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.juul.kable.gatt.GattStatus
import kotlin.uuid.toKotlinUuid

internal actual val LOG_INDENT: String? = null

internal fun LogMessage.detail(status: GattStatus) {
    detail("status", status.toString())
}

internal fun LogMessage.detail(characteristic: BluetoothGattCharacteristic) {
    detail(
        characteristic.service.uuid.toKotlinUuid(),
        characteristic.uuid.toKotlinUuid(),
    )
}

internal fun LogMessage.detail(descriptor: BluetoothGattDescriptor) {
    detail(
        descriptor.characteristic.service.uuid.toKotlinUuid(),
        descriptor.characteristic.uuid.toKotlinUuid(),
        descriptor.uuid.toKotlinUuid(),
    )
}
