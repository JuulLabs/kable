@file:JvmName("ProfileAndroid")

package com.juul.kable

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.juul.kable.Characteristic.Properties
import kotlin.uuid.toKotlinUuid

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformService = BluetoothGattService

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformCharacteristic = BluetoothGattCharacteristic

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformDescriptor = BluetoothGattDescriptor

internal actual class PlatformDiscoveredService(
    actual val service: PlatformService,
) : DiscoveredService {

    actual override val characteristics = service.characteristics.map(::PlatformDiscoveredCharacteristic)
    actual override val serviceUuid get() = service.uuid.toKotlinUuid()
    val instanceId get() = service.instanceId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PlatformDiscoveredService
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
) : DiscoveredCharacteristic {

    actual override val descriptors = characteristic.descriptors.map(::PlatformDiscoveredDescriptor)
    actual override val serviceUuid get() = characteristic.service.uuid.toKotlinUuid()
    val serviceInstanceId get() = characteristic.service.instanceId
    actual override val characteristicUuid get() = characteristic.uuid.toKotlinUuid()
    val instanceId get() = characteristic.instanceId
    actual override val properties get() = Properties(characteristic.properties)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PlatformDiscoveredCharacteristic
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
) : DiscoveredDescriptor {

    actual override val serviceUuid get() = descriptor.characteristic.service.uuid.toKotlinUuid()
    val serviceInstanceId get() = descriptor.characteristic.service.instanceId
    actual override val characteristicUuid get() = descriptor.characteristic.uuid.toKotlinUuid()
    val characteristicInstanceId get() = descriptor.characteristic.instanceId
    actual override val descriptorUuid get() = descriptor.uuid.toKotlinUuid()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PlatformDiscoveredDescriptor
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

internal fun PlatformCharacteristic.toLazyCharacteristic() = LazyCharacteristic(
    serviceUuid = service.uuid.toKotlinUuid(),
    characteristicUuid = uuid.toKotlinUuid(),
)
