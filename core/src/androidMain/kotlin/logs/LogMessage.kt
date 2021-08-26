@file:JvmName("AndroidLogMessage")

package com.juul.kable.logs

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.juul.kable.gatt.GattStatus

internal actual val LOG_INDENT: String? = null

internal fun LogMessage.detail(status: GattStatus) {
    detail("status", status.toString())
}

internal fun LogMessage.detail(service: BluetoothGattService) {
    detail("service", service.uuid.toString())
}

internal fun LogMessage.detail(characteristic: BluetoothGattCharacteristic) {
    detail(characteristic.service)
    detail("characteristic", characteristic.uuid.toString())
}

internal fun LogMessage.detail(descriptor: BluetoothGattDescriptor) {
    detail(descriptor.characteristic)
    detail("descriptor", descriptor.uuid.toString())
}
