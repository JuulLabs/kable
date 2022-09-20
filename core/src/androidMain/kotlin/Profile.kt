@file:JvmName("ProfileAndroid")

package com.juul.kable

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.benasher44.uuid.Uuid
import com.juul.kable.Characteristic.Properties

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

    override val serviceUuid: Uuid get() = service.uuid
    val instanceId: Int get() = service.instanceId
}

public actual data class DiscoveredCharacteristic internal constructor(
    internal actual val characteristic: PlatformCharacteristic,
) : Characteristic {

    public actual val descriptors: List<DiscoveredDescriptor> =
        characteristic.descriptors.map(::DiscoveredDescriptor)

    override val serviceUuid: Uuid get() = characteristic.service.uuid
    override val characteristicUuid: Uuid get() = characteristic.uuid
    val instanceId: Int get() = characteristic.instanceId
    actual val properties: Properties get() = Properties(characteristic.properties)
}

public actual data class DiscoveredDescriptor internal constructor(
    internal actual val descriptor: PlatformDescriptor,
) : Descriptor {

    override val serviceUuid: Uuid get() = descriptor.characteristic.service.uuid
    override val characteristicUuid: Uuid get() = descriptor.characteristic.uuid
    override val descriptorUuid: Uuid get() = descriptor.uuid
}

internal fun PlatformCharacteristic.toLazyCharacteristic() = LazyCharacteristic(
    serviceUuid = service.uuid,
    characteristicUuid = uuid,
)
