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

internal actual class PlatformDiscoveredService internal constructor(
    actual val service: PlatformService,

    /**
     * Artificial "instance id" of this service. Core Bluetooth (unlike Android) does not provide
     * instance ids, so an instance id is generated during service discovery as the position
     * (index) of this service within the peripheral's service list. Allows multiple services with
     * the same UUID to be distinguished from each other.
     */
    val instanceId: Int,
) : DiscoveredService {

    actual override val serviceUuid = service.UUID.toUuid()

    actual override val characteristics =
        service.characteristics
            .orEmpty()
            .mapIndexed { index, characteristic ->
                PlatformDiscoveredCharacteristic(
                    characteristic = characteristic as PlatformCharacteristic,
                    discoveredService = this,
                    instanceId = index,
                )
            }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlatformDiscoveredService) return false
        if (serviceUuid != other.serviceUuid) return false
        if (instanceId != other.instanceId) return false
        return true
    }

    override fun hashCode(): Int {
        var result = serviceUuid.hashCode()
        result = 31 * result + instanceId
        return result
    }

    override fun toString() = "DiscoveredService(serviceUuid=$serviceUuid, instanceId=$instanceId)"
}

internal actual class PlatformDiscoveredCharacteristic internal constructor(
    actual val characteristic: PlatformCharacteristic,
    private val discoveredService: PlatformDiscoveredService,

    /**
     * Artificial "instance id" of this characteristic. Core Bluetooth (unlike Android) does not
     * provide instance ids, so an instance id is generated during service discovery as the
     * position (index) of this characteristic within its parent service's characteristic list.
     * Allows multiple characteristics with the same UUID to be distinguished from each other.
     */
    val instanceId: Int,
) : DiscoveredCharacteristic {

    actual override val serviceUuid: Uuid get() = discoveredService.serviceUuid
    val serviceInstanceId: Int get() = discoveredService.instanceId
    actual override val characteristicUuid = characteristic.UUID.toUuid()
    actual override val properties = Properties(characteristic.properties.toInt())

    actual override val descriptors =
        characteristic.descriptors
            .orEmpty()
            .map { descriptor ->
                PlatformDiscoveredDescriptor(
                    descriptor = descriptor as PlatformDescriptor,
                    discoveredCharacteristic = this,
                )
            }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlatformDiscoveredCharacteristic) return false
        if (serviceUuid != other.serviceUuid) return false
        if (serviceInstanceId != other.serviceInstanceId) return false
        if (characteristicUuid != other.characteristicUuid) return false
        if (instanceId != other.instanceId) return false
        return true
    }

    override fun hashCode(): Int {
        var result = serviceUuid.hashCode()
        result = 31 * result + serviceInstanceId
        result = 31 * result + characteristicUuid.hashCode()
        result = 31 * result + instanceId
        return result
    }

    override fun toString() =
        "DiscoveredCharacteristic(serviceUuid=$serviceUuid, serviceInstanceId=$serviceInstanceId, characteristicUuid=$characteristicUuid, instanceId=$instanceId)"
}

internal actual class PlatformDiscoveredDescriptor internal constructor(
    actual val descriptor: PlatformDescriptor,
    private val discoveredCharacteristic: PlatformDiscoveredCharacteristic,
) : DiscoveredDescriptor {

    actual override val serviceUuid: Uuid get() = discoveredCharacteristic.serviceUuid
    val serviceInstanceId: Int get() = discoveredCharacteristic.serviceInstanceId
    actual override val characteristicUuid: Uuid get() = discoveredCharacteristic.characteristicUuid
    val characteristicInstanceId: Int get() = discoveredCharacteristic.instanceId
    actual override val descriptorUuid = descriptor.UUID.toUuid()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlatformDiscoveredDescriptor) return false
        if (serviceUuid != other.serviceUuid) return false
        if (serviceInstanceId != other.serviceInstanceId) return false
        if (characteristicUuid != other.characteristicUuid) return false
        if (characteristicInstanceId != other.characteristicInstanceId) return false
        if (descriptorUuid != other.descriptorUuid) return false
        return true
    }

    override fun hashCode(): Int {
        var result = serviceUuid.hashCode()
        result = 31 * result + serviceInstanceId
        result = 31 * result + characteristicUuid.hashCode()
        result = 31 * result + characteristicInstanceId
        result = 31 * result + descriptorUuid.hashCode()
        return result
    }

    override fun toString() =
        "DiscoveredDescriptor(serviceUuid=$serviceUuid, serviceInstanceId=$serviceInstanceId, characteristicUuid=$characteristicUuid, characteristicInstanceId=$characteristicInstanceId, descriptorUuid=$descriptorUuid)"
}

/**
 * Finds (by object reference) the [PlatformDiscoveredCharacteristic] that wraps [characteristic],
 * or `null` if [characteristic] is not found in the discovered services.
 */
internal fun List<PlatformDiscoveredService>.findCharacteristic(
    characteristic: PlatformCharacteristic,
): PlatformDiscoveredCharacteristic? {
    forEach { service ->
        service.characteristics.forEach { discoveredCharacteristic ->
            if (discoveredCharacteristic.characteristic === characteristic) {
                return discoveredCharacteristic
            }
        }
    }
    return null
}

internal fun PlatformCharacteristic.toLazyCharacteristic() = LazyCharacteristic(
    serviceUuid = service!!.UUID.toUuid(),
    characteristicUuid = UUID.toUuid(),
)
