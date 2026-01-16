package com.juul.kable

import com.juul.kable.Characteristic.Properties
import com.juul.kable.external.BluetoothCharacteristicProperties
import com.juul.kable.external.BluetoothRemoteGATTCharacteristic
import com.juul.kable.external.BluetoothRemoteGATTDescriptor
import com.juul.kable.external.BluetoothRemoteGATTService
import com.juul.kable.logs.Logger
import com.juul.kable.logs.detail
import com.juul.kable.interop.await
import kotlin.js.toList

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformService = BluetoothRemoteGATTService

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformCharacteristic = BluetoothRemoteGATTCharacteristic

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformDescriptor = BluetoothRemoteGATTDescriptor
private typealias PlatformProperties = BluetoothCharacteristicProperties

internal actual class PlatformDiscoveredService internal constructor(
    actual val service: PlatformService,
    actual override val characteristics: List<PlatformDiscoveredCharacteristic>,
) : DiscoveredService {

    actual override val serviceUuid = service.uuid.toUuid()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlatformDiscoveredService) return false
        return service === other.service
    }

    override fun hashCode() = service.hashCode()

    override fun toString() = "DiscoveredService(serviceUuid=$serviceUuid, hashCode=${hashCode()})"
}

internal actual class PlatformDiscoveredCharacteristic internal constructor(
    actual val characteristic: PlatformCharacteristic,
    actual override val descriptors: List<PlatformDiscoveredDescriptor>,
) : DiscoveredCharacteristic {

    actual override val serviceUuid = characteristic.service.uuid.toUuid()
    actual override val characteristicUuid = characteristic.uuid.toUuid()
    actual override val properties = Properties(characteristic.properties)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlatformDiscoveredCharacteristic) return false
        return characteristic === other.characteristic
    }

    override fun hashCode() = characteristic.hashCode()

    override fun toString() =
        "DiscoveredService(serviceUuid=$serviceUuid, serviceHashCode=${characteristic.service.hashCode()}, characteristicUuid=$characteristicUuid, characteristicHashCode=${hashCode()})"
}

internal actual class PlatformDiscoveredDescriptor internal constructor(
    actual val descriptor: PlatformDescriptor,
) : DiscoveredDescriptor {

    actual override val serviceUuid = descriptor.characteristic.service.uuid.toUuid()
    actual override val characteristicUuid = descriptor.characteristic.uuid.toUuid()
    actual override val descriptorUuid = descriptor.uuid.toUuid()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlatformDiscoveredDescriptor) return false
        return descriptor === other.descriptor
    }

    override fun hashCode() = descriptor.hashCode()

    override fun toString() =
        "DiscoveredService(serviceUuid=$serviceUuid, serviceHashCode=${descriptor.characteristic.service.hashCode()}, characteristicUuid=$characteristicUuid, characteristicHashCode=${descriptor.characteristic.hashCode()}, descriptorUuid=$descriptorUuid)"
}

internal suspend fun PlatformService.toDiscoveredService(logger: Logger): PlatformDiscoveredService {
    val characteristics = getCharacteristics()
        .await()
        .toList()
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
        .map { it.toList() }
        .getOrDefault(emptyList())
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
