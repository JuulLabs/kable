package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.BluetoothRemoteGATTService
import com.juul.kable.logs.Logger
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

internal suspend fun BluetoothRemoteGATTService.toPlatformService(logger: Logger): PlatformService {
    val serviceUuid = uuid.toUuid()
    val characteristics = getCharacteristics()
        .await()
        .map { characteristic ->
            characteristic.toPlatformCharacteristic(serviceUuid, logger)
        }

    return PlatformService(
        serviceUuid = serviceUuid,
        characteristics = characteristics,
        bluetoothRemoteGATTService = this,
    )
}

/** @throws NoSuchElementException if service or characteristic is not found. */
internal fun List<PlatformService>.findCharacteristic(
    characteristic: Characteristic
): PlatformCharacteristic =
    findCharacteristic(
        serviceUuid = characteristic.serviceUuid,
        characteristicUuid = characteristic.characteristicUuid
    )

/** @throws NoSuchElementException if service or characteristic is not found. */
private fun List<PlatformService>.findCharacteristic(
    serviceUuid: Uuid,
    characteristicUuid: Uuid
): PlatformCharacteristic =
    first(serviceUuid)
        .characteristics
        .first(characteristicUuid)

/** @throws NoSuchElementException if service, characteristic or descriptor is not found. */
internal fun List<PlatformService>.findDescriptor(
    descriptor: Descriptor
): PlatformDescriptor =
    findDescriptor(
        serviceUuid = descriptor.serviceUuid,
        characteristicUuid = descriptor.characteristicUuid,
        descriptorUuid = descriptor.descriptorUuid
    )

/** @throws NoSuchElementException if service, characteristic or descriptor is not found. */
private fun List<PlatformService>.findDescriptor(
    serviceUuid: Uuid,
    characteristicUuid: Uuid,
    descriptorUuid: Uuid
): PlatformDescriptor =
    findCharacteristic(
        serviceUuid = serviceUuid,
        characteristicUuid = characteristicUuid
    ).descriptors.first(descriptorUuid)
