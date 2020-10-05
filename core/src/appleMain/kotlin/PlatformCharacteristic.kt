package com.juul.kable

import com.benasher44.uuid.Uuid
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBDescriptor
import platform.Foundation.NSLog

internal data class PlatformCharacteristic(
    override val serviceUuid: Uuid,
    override val characteristicUuid: Uuid,
    val cbCharacteristic: CBCharacteristic,
    val descriptors: List<PlatformDescriptor>,
) : Characteristic

internal fun PlatformCharacteristic.toDiscoveredCharacteristic() = DiscoveredCharacteristic(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptors = descriptors.map { it.toLazyDescriptor() },
)

internal fun CBCharacteristic.toPlatformCharacteristic(
    serviceUuid: Uuid,
): PlatformCharacteristic {
    val characteristicUuid = UUID.toUuid()
    val platformDescriptors = descriptors?.map { descriptor ->
        NSLog("%@", descriptor)
        descriptor as CBDescriptor
        descriptor.toPlatformDescriptor(serviceUuid, characteristicUuid)
    } ?: emptyList()

    return PlatformCharacteristic(
        serviceUuid = serviceUuid,
        characteristicUuid = characteristicUuid,
        descriptors = platformDescriptors,
        cbCharacteristic = this,
    )
}
