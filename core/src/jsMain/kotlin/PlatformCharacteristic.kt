package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.BluetoothRemoteGATTCharacteristic
import kotlinx.coroutines.await

internal data class PlatformCharacteristic(
    override val serviceUuid: Uuid,
    override val characteristicUuid: Uuid,
    val bluetoothRemoteGATTCharacteristic: BluetoothRemoteGATTCharacteristic,
    val descriptors: List<PlatformDescriptor>,
) : Characteristic

internal fun PlatformCharacteristic.toDiscoveredCharacteristic() = DiscoveredCharacteristic(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptors = descriptors.map { it.toLazyDescriptor() },
)

internal suspend fun BluetoothRemoteGATTCharacteristic.toPlatformCharacteristic(
    serviceUuid: Uuid,
    logger: Logger,
): PlatformCharacteristic {
    val characteristicUuid = uuid.toUuid()
    val descriptors = runCatching { getDescriptors().await() }
        .onFailure { cause ->
            logger.error(cause) {
                message = "Unable to retrieve descriptor."
                detail(this@toPlatformCharacteristic)
            }
        }
        .getOrDefault(emptyArray())
    val platformDescriptors = descriptors.map { descriptor ->
        descriptor.toPlatformDescriptor(serviceUuid, characteristicUuid)
    }

    return PlatformCharacteristic(
        serviceUuid = serviceUuid,
        characteristicUuid = characteristicUuid,
        descriptors = platformDescriptors,
        bluetoothRemoteGATTCharacteristic = this,
    )
}
