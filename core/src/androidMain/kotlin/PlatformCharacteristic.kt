package com.juul.kable

import android.bluetooth.BluetoothGattCharacteristic
import com.benasher44.uuid.Uuid

@Suppress("PROPERTY_TYPE_MISMATCH_ON_OVERRIDE") // https://youtrack.jetbrains.com/issue/KTIJ-405
internal data class PlatformCharacteristic(
    override val serviceUuid: Uuid,
    override val characteristicUuid: Uuid,
    val bluetoothGattCharacteristic: BluetoothGattCharacteristic,
    val descriptors: List<PlatformDescriptor>,
) : Characteristic {
    override fun toString(): String =
        "PlatformCharacteristic(serviceUuid=$serviceUuid, characteristicUuid=$characteristicUuid)"
}

internal fun PlatformCharacteristic.toDiscoveredCharacteristic() = DiscoveredCharacteristic(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptors = descriptors.map { it.toLazyDescriptor() },
)

internal fun BluetoothGattCharacteristic.toPlatformCharacteristic(): PlatformCharacteristic {
    val platformDescriptors = descriptors.map { descriptor ->
        descriptor.toPlatformDescriptor(service.uuid, uuid)
    }

    return PlatformCharacteristic(
        serviceUuid = service.uuid,
        characteristicUuid = uuid,
        descriptors = platformDescriptors,
        bluetoothGattCharacteristic = this,
    )
}

internal fun BluetoothGattCharacteristic.toLazyCharacteristic() = LazyCharacteristic(
    serviceUuid = service.uuid,
    characteristicUuid = uuid,
)
