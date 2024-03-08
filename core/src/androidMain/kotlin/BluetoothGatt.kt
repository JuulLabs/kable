package com.juul.kable

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothStatusCodes.ERROR_GATT_WRITE_NOT_ALLOWED
import android.bluetooth.BluetoothStatusCodes.ERROR_GATT_WRITE_REQUEST_BUSY
import android.bluetooth.BluetoothStatusCodes.ERROR_MISSING_BLUETOOTH_CONNECT_PERMISSION
import android.bluetooth.BluetoothStatusCodes.ERROR_PROFILE_SERVICE_NOT_BOUND
import android.bluetooth.BluetoothStatusCodes.SUCCESS
import android.os.Build
import com.juul.kable.AndroidPeripheral.WriteResult

internal fun BluetoothGatt.discoverServicesOrThrow() {
    if (!discoverServices()) {
        throw GattRequestRejectedException()
    }
}

internal fun BluetoothGatt.setCharacteristicNotificationOrThrow(
    characteristic: PlatformCharacteristic,
    enable: Boolean,
) {
    if (!setCharacteristicNotification(characteristic, enable)) {
        throw GattRequestRejectedException()
    }
}

internal fun BluetoothGatt.readCharacteristicOrThrow(
    characteristic: PlatformCharacteristic,
) {
    if (!readCharacteristic(characteristic)) {
        throw GattRequestRejectedException()
    }
}

internal fun BluetoothGatt.readDescriptorOrThrow(
    descriptor: PlatformDescriptor,
) {
    if (!readDescriptor(descriptor)) {
        throw GattRequestRejectedException()
    }
}

internal fun BluetoothGatt.readRemoteRssiOrThrow() {
    if (!readRemoteRssi()) {
        throw GattRequestRejectedException()
    }
}

@Suppress("DEPRECATION")
internal fun BluetoothGatt.writeCharacteristicOrThrow(
    characteristic: PlatformCharacteristic,
    data: ByteArray,
    writeType: Int,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val result = writeCharacteristic(characteristic, data, writeType)
        if (result != SUCCESS) {
            throw GattWriteException(writeResultFrom(result))
        }
    } else {
        characteristic.value = data
        characteristic.writeType = writeType
        if (!writeCharacteristic(characteristic)) {
            throw GattWriteException(WriteResult.Unknown)
        }
    }
}

@Suppress("DEPRECATION")
internal fun BluetoothGatt.writeDescriptorOrThrow(
    descriptor: PlatformDescriptor,
    data: ByteArray,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val result = writeDescriptor(descriptor, data)
        if (result != SUCCESS) {
            throw GattWriteException(writeResultFrom(result))
        }
    } else {
        descriptor.value = data
        if (!writeDescriptor(descriptor)) {
            throw GattRequestRejectedException()
        }
    }
}

/**
 * Possible return value of [BluetoothGatt.writeCharacteristic] or [BluetoothGatt.writeDescriptor],
 * yet marked as `@hide` in Android source:
 * https://cs.android.com/android/platform/superproject/main/+/b7a389a145ff443550e1a942bf713c60c2bd6a14:packages/modules/Bluetooth/framework/java/android/bluetooth/BluetoothStatusCodes.java;l=45-50
 */
private const val ERROR_DEVICE_NOT_CONNECTED = 4

private fun writeResultFrom(value: Int): WriteResult = when (value) {
    ERROR_DEVICE_NOT_CONNECTED -> WriteResult.NotConnected
    ERROR_GATT_WRITE_NOT_ALLOWED -> WriteResult.WriteNotAllowed
    ERROR_GATT_WRITE_REQUEST_BUSY -> WriteResult.WriteRequestBusy
    ERROR_MISSING_BLUETOOTH_CONNECT_PERMISSION -> WriteResult.MissingBluetoothConnectPermission
    ERROR_PROFILE_SERVICE_NOT_BOUND -> WriteResult.ProfileServiceNotBound
    else -> WriteResult.Unknown
}
