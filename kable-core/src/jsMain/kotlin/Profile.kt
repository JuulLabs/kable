@file:OptIn(ExperimentalUuidApi::class)

package com.juul.kable

import com.juul.kable.Characteristic.Properties
import com.juul.kable.external.BluetoothCharacteristicProperties
import com.juul.kable.external.BluetoothRemoteGATTCharacteristic
import com.juul.kable.external.BluetoothRemoteGATTDescriptor
import com.juul.kable.external.BluetoothRemoteGATTService
import com.juul.kable.logs.Logger
import com.juul.kable.logs.detail
import kotlinx.coroutines.await
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformService = BluetoothRemoteGATTService

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformCharacteristic = BluetoothRemoteGATTCharacteristic

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformDescriptor = BluetoothRemoteGATTDescriptor
private typealias PlatformProperties = BluetoothCharacteristicProperties

public actual data class DiscoveredService internal constructor(
    internal actual val service: PlatformService,
    public actual val characteristics: List<DiscoveredCharacteristic>,
) : Service {

    actual override val serviceUuid: Uuid = service.uuid.toUuid()
}

public actual data class DiscoveredCharacteristic internal constructor(
    internal actual val characteristic: PlatformCharacteristic,
    public actual val descriptors: List<DiscoveredDescriptor>,
) : Characteristic {

    actual override val serviceUuid: Uuid = characteristic.service.uuid.toUuid()
    actual override val characteristicUuid: Uuid = characteristic.uuid.toUuid()

    public actual val properties: Properties = Properties(characteristic.properties)
}

public actual data class DiscoveredDescriptor internal constructor(
    internal actual val descriptor: PlatformDescriptor,
) : Descriptor {

    actual override val serviceUuid: Uuid = descriptor.characteristic.service.uuid.toUuid()
    actual override val characteristicUuid: Uuid = descriptor.characteristic.uuid.toUuid()
    actual override val descriptorUuid: Uuid = descriptor.uuid.toUuid()
}

internal suspend fun PlatformService.toDiscoveredService(logger: Logger): DiscoveredService {
    val characteristics = getCharacteristics()
        .await()
        .map { characteristic ->
            characteristic.toDiscoveredCharacteristic(logger)
        }

    return DiscoveredService(
        service = this,
        characteristics = characteristics,
    )
}

private suspend fun BluetoothRemoteGATTCharacteristic.toDiscoveredCharacteristic(
    logger: Logger,
): DiscoveredCharacteristic {
    val descriptors = runCatching { getDescriptors().await() }
        .onFailure {
            logger.warn {
                message = "Unable to retrieve descriptor."
                detail(this@toDiscoveredCharacteristic)
            }
        }
        .getOrDefault(emptyArray())
    val platformDescriptors = descriptors.map(::DiscoveredDescriptor)

    return DiscoveredCharacteristic(
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
