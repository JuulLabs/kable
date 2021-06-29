package com.juul.kable

import android.bluetooth.BluetoothGattService
import com.benasher44.uuid.Uuid

@Suppress("PROPERTY_TYPE_MISMATCH_ON_OVERRIDE") // https://youtrack.jetbrains.com/issue/KTIJ-405
internal data class PlatformService(
    override val serviceUuid: Uuid,
    val bluetoothGattService: BluetoothGattService,
    val characteristics: List<PlatformCharacteristic>,
) : Service

internal fun PlatformService.toDiscoveredService() = DiscoveredService(
    serviceUuid = serviceUuid,
    characteristics = characteristics.map { it.toDiscoveredCharacteristic() },
)

internal fun BluetoothGattService.toPlatformService(): PlatformService {
    val serviceUuid = uuid
    val characteristics = characteristics
        .map { characteristic -> characteristic.toPlatformCharacteristic() }

    return PlatformService(
        serviceUuid = serviceUuid,
        characteristics = characteristics,
        bluetoothGattService = this,
    )
}

/** @throws NoSuchElementException if service or characteristic is not found. */
internal fun List<PlatformService>.findCharacteristic(
    characteristic: Characteristic, propertyMask: Int = Int.MIN_VALUE
): PlatformCharacteristic =
    findCharacteristic(
        serviceUuid = characteristic.serviceUuid,
        characteristicUuid = characteristic.characteristicUuid,
        propertyMask = propertyMask
    )

private fun List<PlatformService>.findCharacteristic(
    serviceUuid: Uuid,
    characteristicUuid: Uuid,
    propertyMask: Int = Int.MIN_VALUE
): PlatformCharacteristic =
    first(serviceUuid)
        .characteristics
        .first { platformCharacteristic ->
            platformCharacteristic.characteristicUuid == characteristicUuid
                    && platformCharacteristic.bluetoothGattCharacteristic.properties and propertyMask != 0
        }

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
        characteristicUuid = characteristicUuid,
    ).descriptors.first(descriptorUuid)
