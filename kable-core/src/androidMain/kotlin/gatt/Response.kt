package com.juul.kable.gatt

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.juul.kable.android.GattStatus

internal sealed class Response {

    abstract val status: GattStatus

    data class OnReadRemoteRssi(
        val rssi: Int,
        override val status: GattStatus,
    ) : Response()

    data class OnServicesDiscovered(
        override val status: GattStatus,
        val services: List<BluetoothGattService>,
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

internal data class OnMtuChanged(
    val mtu: Int,
    override val status: GattStatus,
) : Response()

internal object OnServiceChanged
