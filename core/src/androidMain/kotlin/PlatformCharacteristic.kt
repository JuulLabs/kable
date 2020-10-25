package com.juul.kable

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.benasher44.uuid.Uuid

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

internal fun BluetoothGattCharacteristic.toPlatformCharacteristic(
    serviceUuid: Uuid,
): PlatformCharacteristic {
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
