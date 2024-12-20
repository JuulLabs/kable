package com.juul.kable

import com.juul.kable.Characteristic.Properties
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBService
import kotlin.uuid.Uuid

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformService = CBService

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformCharacteristic = CBCharacteristic

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformDescriptor = CBDescriptor

public actual data class DiscoveredService internal constructor(
    internal actual val service: PlatformService,
) : Service {

    public actual val characteristics: List<DiscoveredCharacteristic> =
        service.characteristics
            .orEmpty()
            .map { it as PlatformCharacteristic }
            .map(::DiscoveredCharacteristic)

    actual override val serviceUuid: Uuid = service.UUID.toUuid()
}

public actual data class DiscoveredCharacteristic internal constructor(
    internal actual val characteristic: PlatformCharacteristic,
) : Characteristic {

    public actual val descriptors: List<DiscoveredDescriptor> =
        characteristic.descriptors
            .orEmpty()
            .map { it as PlatformDescriptor }
            .map(::DiscoveredDescriptor)

    actual override val serviceUuid: Uuid = characteristic.service!!.UUID.toUuid()
    actual override val characteristicUuid: Uuid = characteristic.UUID.toUuid()

    public actual val properties: Properties = Properties(characteristic.properties.toInt())
}

public actual data class DiscoveredDescriptor internal constructor(
    internal actual val descriptor: PlatformDescriptor,
) : Descriptor {

    actual override val serviceUuid: Uuid = descriptor.characteristic!!.service!!.UUID.toUuid()
    actual override val characteristicUuid: Uuid = descriptor.characteristic!!.UUID.toUuid()
    actual override val descriptorUuid: Uuid = descriptor.UUID.toUuid()
}

internal fun PlatformCharacteristic.toLazyCharacteristic() = LazyCharacteristic(
    serviceUuid = service!!.UUID.toUuid(),
    characteristicUuid = UUID.toUuid(),
)
