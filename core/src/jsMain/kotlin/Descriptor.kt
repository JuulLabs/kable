package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.BluetoothRemoteGATTDescriptor

public actual class Descriptor internal constructor(
    internal val bluetoothRemoteGATTDescriptor: BluetoothRemoteGATTDescriptor,
) {

    public actual val uuid: Uuid by lazy { bluetoothRemoteGATTDescriptor.uuid.toUuid() }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false
        other as Descriptor
        if (uuid != other.uuid) return false
        return true
    }

    override fun hashCode(): Int = uuid.hashCode()
}
