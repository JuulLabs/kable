package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.BluetoothRemoteGATTService
import kotlinx.coroutines.await

internal data class PlatformService(
    override val serviceUuid: Uuid,
    val bluetoothRemoteGATTService: BluetoothRemoteGATTService,
    val characteristics: List<PlatformCharacteristic>,
) : Service

internal fun PlatformService.toDiscoveredService() = DiscoveredService(
    serviceUuid = serviceUuid,
    characteristics = characteristics.map { it.toDiscoveredCharacteristic() },
)

internal suspend fun BluetoothRemoteGATTService.toPlatformService(): PlatformService {
    val serviceUuid = uuid.toUuid()
    val characteristics = getCharacteristics()
        .await()
        .map { characteristic ->
            console.dir(characteristic)
            characteristic.toPlatformCharacteristic(serviceUuid)
        }

    return PlatformService(
        serviceUuid = serviceUuid,
        characteristics = characteristics,
        bluetoothRemoteGATTService = this,
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
