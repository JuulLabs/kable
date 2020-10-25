package com.juul.kable

import android.bluetooth.BluetoothGattDescriptor
import com.benasher44.uuid.Uuid

internal data class PlatformDescriptor(
    override val serviceUuid: Uuid,
    override val characteristicUuid: Uuid,
    override val descriptorUuid: Uuid,
    val bluetoothGattDescriptor: BluetoothGattDescriptor,
) : Descriptor

internal fun PlatformDescriptor.toLazyDescriptor() = LazyDescriptor(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptorUuid = descriptorUuid,
)

internal fun BluetoothGattDescriptor.toPlatformDescriptor(
    serviceUuid: Uuid,
    characteristicUuid: Uuid,
) = PlatformDescriptor(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptorUuid = uuid.toUuid(),
    bluetoothGattDescriptor = this,
)
