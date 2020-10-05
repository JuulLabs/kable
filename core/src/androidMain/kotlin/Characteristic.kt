package com.juul.kable

import android.bluetooth.BluetoothGattCharacteristic
import com.benasher44.uuid.Uuid

public actual data class Characteristic(
    internal val serviceUuid: Uuid,
    internal val bluetoothGattCharacteristic: BluetoothGattCharacteristic,
) {

    public actual val uuid: Uuid by lazy { bluetoothGattCharacteristic.uuid.toUuid() }

    public actual val descriptors: List<Descriptor>
        get() = bluetoothGattCharacteristic.descriptors?.map { descriptor ->
            Descriptor(descriptor)
        } ?: emptyList()
}
