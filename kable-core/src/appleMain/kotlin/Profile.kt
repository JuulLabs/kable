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

internal actual class PlatformDiscoveredService internal constructor(
    actual val service: PlatformService,
) : DiscoveredService {

    actual override val characteristics =
        service.characteristics
            .orEmpty()
            .map { it as PlatformCharacteristic }
            .map(::PlatformDiscoveredCharacteristic)

    actual override val serviceUuid = service.UUID.toUuid()

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
) : DiscoveredCharacteristic {

    private val cbService = characteristic.service!!

    actual override val descriptors =
        characteristic.descriptors
            .orEmpty()
            .map { it as PlatformDescriptor }
            .map(::PlatformDiscoveredDescriptor)

    actual override val serviceUuid = cbService.UUID.toUuid()
    actual override val characteristicUuid = characteristic.UUID.toUuid()
    actual override val properties = Properties(characteristic.properties.toInt())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlatformDiscoveredCharacteristic) return false
        return characteristic === other.characteristic
    }

    override fun hashCode() = characteristic.hashCode()

    override fun toString() =
        "DiscoveredCharacteristic(serviceUuid=$serviceUuid, serviceHashCode=${cbService.hashCode()}, characteristicUuid=$characteristicUuid, characteristicHashCode=${hashCode()})"
}

internal actual class PlatformDiscoveredDescriptor internal constructor(
    actual val descriptor: PlatformDescriptor,
) : DiscoveredDescriptor {

    private val cbCharacteristic = descriptor.characteristic!!
    private val cbService = cbCharacteristic.service!!

    actual override val serviceUuid = cbService.UUID.toUuid()
    actual override val characteristicUuid = cbCharacteristic.UUID.toUuid()
    actual override val descriptorUuid = descriptor.UUID.toUuid()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlatformDiscoveredDescriptor) return false
        return descriptor === other.descriptor
    }

    override fun hashCode() = descriptor.hashCode()

    override fun toString() =
        "DiscoveredService(serviceUuid=$serviceUuid, serviceHashCode=${cbService.hashCode()}, characteristicUuid=$characteristicUuid, characteristicHashCode=${cbCharacteristic.hashCode()}, descriptorUuid=$descriptorUuid)"
}

internal fun PlatformCharacteristic.toLazyCharacteristic() = LazyCharacteristic(
    serviceUuid = service!!.UUID.toUuid(),
    characteristicUuid = UUID.toUuid(),
)
