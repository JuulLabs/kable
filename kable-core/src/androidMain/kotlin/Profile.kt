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

internal actual data class PlatformDiscoveredService(
    actual val service: PlatformService,
) : DiscoveredService {

    actual override val characteristics = service.characteristics.map(::PlatformDiscoveredCharacteristic)
    override val serviceUuid get() = service.uuid.toKotlinUuid()
    val instanceId: Int get() = service.instanceId
}

internal actual data class PlatformDiscoveredCharacteristic internal constructor(
    internal actual val characteristic: PlatformCharacteristic,
) : DiscoveredCharacteristic {

    actual override val descriptors = characteristic.descriptors.map(::PlatformDiscoveredDescriptor)
    override val serviceUuid get() = characteristic.service.uuid.toKotlinUuid()
    override val characteristicUuid get() = characteristic.uuid.toKotlinUuid()
    val instanceId: Int get() = characteristic.instanceId
    override val properties get() = Properties(characteristic.properties)
}

internal actual data class PlatformDiscoveredDescriptor internal constructor(
    internal actual val descriptor: PlatformDescriptor,
) : DiscoveredDescriptor {

    override val serviceUuid get() = descriptor.characteristic.service.uuid.toKotlinUuid()
    override val characteristicUuid get() = descriptor.characteristic.uuid.toKotlinUuid()
    override val descriptorUuid get() = descriptor.uuid.toKotlinUuid()
}

internal fun PlatformCharacteristic.toLazyCharacteristic() = LazyCharacteristic(
    serviceUuid = service.uuid.toKotlinUuid(),
    characteristicUuid = uuid.toKotlinUuid(),
)
