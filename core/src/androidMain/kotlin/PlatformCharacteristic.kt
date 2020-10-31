package com.juul.kable

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.benasher44.uuid.Uuid

@Suppress("PROPERTY_TYPE_MISMATCH_ON_OVERRIDE") // https://youtrack.jetbrains.com/issue/KTIJ-405
internal data class PlatformCharacteristic(
    override val serviceUuid: Uuid,
    override val characteristicUuid: Uuid,
    val bluetoothGattCharacteristic: BluetoothGattCharacteristic,
    val descriptors: List<PlatformDescriptor>,
) : Characteristic

internal fun PlatformCharacteristic.toDiscoveredCharacteristic() = DiscoveredCharacteristic(
    serviceUuid = serviceUuid,
    characteristicUuid = characteristicUuid,
    descriptors = descriptors.map { it.toLazyDescriptor() },
)

internal fun BluetoothGattCharacteristic.toPlatformCharacteristic(): PlatformCharacteristic {
    val serviceUuid = service.uuid.toUuid()
    val characteristicUuid = uuid.toUuid()
    val platformDescriptors = descriptors.map { descriptor ->
        Log.d(TAG, descriptor.toString())
        descriptor.toPlatformDescriptor(serviceUuid, characteristicUuid)
    }

    return PlatformCharacteristic(
        serviceUuid = serviceUuid,
        characteristicUuid = characteristicUuid,
        descriptors = platformDescriptors,
        bluetoothGattCharacteristic = this,
    )
}

internal fun BluetoothGattCharacteristic.toLazyCharacteristic() = LazyCharacteristic(
    serviceUuid = service.uuid.toUuid(),
    characteristicUuid = uuid.toUuid(),
)
