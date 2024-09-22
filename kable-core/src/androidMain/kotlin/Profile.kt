@file:JvmName("ProfileAndroid")
@file:OptIn(ExperimentalUuidApi::class)

package com.juul.kable

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.juul.kable.Characteristic.Properties
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformService = BluetoothGattService

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformCharacteristic = BluetoothGattCharacteristic

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias PlatformDescriptor = BluetoothGattDescriptor

public actual data class DiscoveredService internal constructor(
    internal actual val service: PlatformService,
) : Service {

    public actual val characteristics: List<DiscoveredCharacteristic> =
        service.characteristics.map(::DiscoveredCharacteristic)

    actual override val serviceUuid: Uuid get() = service.uuid.toKotlinUuid()
    val instanceId: Int get() = service.instanceId
}

public actual data class DiscoveredCharacteristic internal constructor(
    internal actual val characteristic: PlatformCharacteristic,
) : Characteristic {

    public actual val descriptors: List<DiscoveredDescriptor> =
        characteristic.descriptors.map(::DiscoveredDescriptor)

    actual override val serviceUuid: Uuid get() = characteristic.service.uuid.toKotlinUuid()
    actual override val characteristicUuid: Uuid get() = characteristic.uuid.toKotlinUuid()
    val instanceId: Int get() = characteristic.instanceId
    public actual val properties: Properties get() = Properties(characteristic.properties)
}

public actual data class DiscoveredDescriptor internal constructor(
    internal actual val descriptor: PlatformDescriptor,
) : Descriptor {

    actual override val serviceUuid: Uuid get() = descriptor.characteristic.service.uuid.toKotlinUuid()
    actual override val characteristicUuid: Uuid get() = descriptor.characteristic.uuid.toKotlinUuid()
    actual override val descriptorUuid: Uuid get() = descriptor.uuid.toKotlinUuid()
}

internal fun PlatformCharacteristic.toLazyCharacteristic() = LazyCharacteristic(
    serviceUuid = service.uuid.toKotlinUuid(),
    characteristicUuid = uuid.toKotlinUuid(),
)
