package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.BluetoothRemoteGATTCharacteristic
import com.juul.kable.external.BluetoothServiceUUID

public actual class Characteristic internal constructor(
    internal val serviceUuid: Uuid,
    internal val bluetoothRemoteGATTCharacteristic: BluetoothRemoteGATTCharacteristic,
) {

    public actual val uuid: Uuid by lazy { bluetoothRemoteGATTCharacteristic.uuid.toUuid() }

    public actual val descriptors: List<Descriptor>
        get() = TODO()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false
        other as Characteristic
        if (uuid != other.uuid) return false
        return true
    }

    override fun hashCode(): Int = uuid.hashCode()
}
