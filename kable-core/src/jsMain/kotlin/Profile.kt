package com.juul.kable

import com.juul.kable.Characteristic.Properties
import com.juul.kable.external.BluetoothCharacteristicProperties
import com.juul.kable.external.BluetoothRemoteGATTCharacteristic
import com.juul.kable.external.BluetoothRemoteGATTDescriptor
import com.juul.kable.external.BluetoothRemoteGATTService
import com.juul.kable.logs.Logger
import com.juul.kable.logs.detail
import kotlinx.coroutines.await

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformService = BluetoothRemoteGATTService

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformCharacteristic = BluetoothRemoteGATTCharacteristic

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformDescriptor = BluetoothRemoteGATTDescriptor
private typealias PlatformProperties = BluetoothCharacteristicProperties

internal actual data class PlatformDiscoveredService internal constructor(
    internal actual val service: PlatformService,
    actual override val characteristics: List<PlatformDiscoveredCharacteristic>,
) : DiscoveredService {

    override val serviceUuid = service.uuid.toUuid()
}

internal actual data class PlatformDiscoveredCharacteristic internal constructor(
    internal actual val characteristic: PlatformCharacteristic,
    actual override val descriptors: List<PlatformDiscoveredDescriptor>,
) : DiscoveredCharacteristic {

    override val serviceUuid = characteristic.service.uuid.toUuid()
    override val characteristicUuid = characteristic.uuid.toUuid()
    override val properties = Properties(characteristic.properties)
}

internal actual data class PlatformDiscoveredDescriptor internal constructor(
    internal actual val descriptor: PlatformDescriptor,
) : DiscoveredDescriptor {

    override val serviceUuid = descriptor.characteristic.service.uuid.toUuid()
    override val characteristicUuid = descriptor.characteristic.uuid.toUuid()
    override val descriptorUuid = descriptor.uuid.toUuid()
}

internal suspend fun PlatformService.toDiscoveredService(logger: Logger): PlatformDiscoveredService {
    val characteristics = getCharacteristics()
        .await()
        .map { characteristic ->
            characteristic.toDiscoveredCharacteristic(logger)
        }

    return PlatformDiscoveredService(
        service = this,
        characteristics = characteristics,
    )
}

private suspend fun BluetoothRemoteGATTCharacteristic.toDiscoveredCharacteristic(
    logger: Logger,
): PlatformDiscoveredCharacteristic {
    val descriptors = runCatching { getDescriptors().await() }
        .onFailure {
            logger.warn {
                message = "Unable to retrieve descriptor."
                detail(this@toDiscoveredCharacteristic)
            }
        }
        .getOrDefault(emptyArray())
    val platformDescriptors = descriptors.map(::PlatformDiscoveredDescriptor)

    return PlatformDiscoveredCharacteristic(
        characteristic = this,
        descriptors = platformDescriptors,
    )
}

private fun Properties(platformProperties: PlatformProperties): Properties {
    var properties = Properties(0)
    with(platformProperties) {
        if (broadcast) properties = properties or Broadcast
        if (read) properties = properties or Read
        if (writeWithoutResponse) properties = properties or WriteWithoutResponse
        if (write) properties = properties or Write
        if (notify) properties = properties or Notify
        if (indicate) properties = properties or Indicate
        if (authenticatedSignedWrites) properties = properties or SignedWrite
    }
    return properties
}
