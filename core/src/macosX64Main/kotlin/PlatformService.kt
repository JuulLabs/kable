package com.juul.kable

import com.benasher44.uuid.Uuid
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBService

internal data class PlatformService(
    override val serviceUuid: Uuid,
    val cbService: CBService,
    val characteristics: List<PlatformCharacteristic>,
) : Service

internal fun PlatformService.toDiscoveredService() = DiscoveredService(
    serviceUuid = serviceUuid,
    characteristics = characteristics.map { it.toDiscoveredCharacteristic() },
)

internal fun CBService.toPlatformService(): PlatformService {
    val serviceUuid = UUID.toUuid()
    val platformCharacteristics = characteristics?.map { characteristic ->
        characteristic as CBCharacteristic
        characteristic.toPlatformCharacteristic(serviceUuid)
    } ?: emptyList()

    return PlatformService(
        serviceUuid = serviceUuid,
        characteristics = platformCharacteristics,
        cbService = this,
    )
}

/** @throws IOException if service or characteristic is not found. */
internal fun List<PlatformService>.findCharacteristic(
    characteristic: Characteristic
) = findCharacteristic(
    serviceUuid = characteristic.serviceUuid,
    characteristicUuid = characteristic.characteristicUuid
)

/** @throws IOException if service or characteristic is not found. */
private fun List<PlatformService>.findCharacteristic(
    serviceUuid: Uuid,
    characteristicUuid: Uuid
): PlatformCharacteristic = this
    .first(serviceUuid)
    .characteristics
    .first(characteristicUuid)

/** @throws IOException if service, characteristic or descriptor is not found. */
internal fun List<PlatformService>.findDescriptor(
    descriptor: Descriptor
) = findDescriptor(
    serviceUuid = descriptor.serviceUuid,
    characteristicUuid = descriptor.characteristicUuid,
    descriptorUuid = descriptor.descriptorUuid
)

/** @throws IOException if service, characteristic or descriptor is not found. */
private fun List<PlatformService>.findDescriptor(
    serviceUuid: Uuid,
    characteristicUuid: Uuid,
    descriptorUuid: Uuid
): PlatformDescriptor =
    this.findCharacteristic(serviceUuid, characteristicUuid)
        .descriptors
        .first(descriptorUuid)
