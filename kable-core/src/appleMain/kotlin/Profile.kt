package com.juul.kable

import com.juul.kable.Characteristic.Properties
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBService

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformService = CBService

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformCharacteristic = CBCharacteristic

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformDescriptor = CBDescriptor

internal actual data class PlatformDiscoveredService internal constructor(
    internal actual val service: PlatformService,
) : DiscoveredService {

    actual override val characteristics =
        service.characteristics
            .orEmpty()
            .map { it as PlatformCharacteristic }
            .map(::PlatformDiscoveredCharacteristic)

    override val serviceUuid = service.UUID.toUuid()
}

internal actual data class PlatformDiscoveredCharacteristic internal constructor(
    internal actual val characteristic: PlatformCharacteristic,
) : DiscoveredCharacteristic {

    actual override val descriptors =
        characteristic.descriptors
            .orEmpty()
            .map { it as PlatformDescriptor }
            .map(::PlatformDiscoveredDescriptor)

    override val serviceUuid = characteristic.service!!.UUID.toUuid()
    override val characteristicUuid = characteristic.UUID.toUuid()

    override val properties = Properties(characteristic.properties.toInt())
}

internal actual data class PlatformDiscoveredDescriptor internal constructor(
    internal actual val descriptor: PlatformDescriptor,
) : DiscoveredDescriptor {

    override val serviceUuid = descriptor.characteristic!!.service!!.UUID.toUuid()
    override val characteristicUuid = descriptor.characteristic!!.UUID.toUuid()
    override val descriptorUuid = descriptor.UUID.toUuid()
}

internal fun PlatformCharacteristic.toLazyCharacteristic() = LazyCharacteristic(
    serviceUuid = service!!.UUID.toUuid(),
    characteristicUuid = UUID.toUuid(),
)
