package com.juul.kable

import com.benasher44.uuid.Uuid
import platform.CoreBluetooth.CBDescriptor

internal data class PlatformDescriptor(
    override val serviceUuid: Uuid,
    override val characteristicUuid: Uuid,
    override val descriptorUuid: Uuid,
    val cbDescriptor: CBDescriptor,
) : Descriptor

internal fun PlatformDescriptor.toLazyDescriptor() = LazyDescriptor(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptorUuid = descriptorUuid,
)

internal fun CBDescriptor.toPlatformDescriptor(
    serviceUuid: Uuid,
    characteristicUuid: Uuid,
) = PlatformDescriptor(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptorUuid = UUID.toUuid(),
    cbDescriptor = this,
)
