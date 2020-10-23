package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.BluetoothRemoteGATTDescriptor

internal data class PlatformDescriptor(
    override val serviceUuid: Uuid,
    override val characteristicUuid: Uuid,
    override val descriptorUuid: Uuid,
    val bluetoothRemoteGATTDescriptor: BluetoothRemoteGATTDescriptor,
) : Descriptor

internal fun PlatformDescriptor.toLazyDescriptor() = LazyDescriptor(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptorUuid = descriptorUuid,
)

internal fun BluetoothRemoteGATTDescriptor.toPlatformDescriptor(
    serviceUuid: Uuid,
    characteristicUuid: Uuid,
) = PlatformDescriptor(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptorUuid = uuid.toUuid(),
    bluetoothRemoteGATTDescriptor = this,
)
