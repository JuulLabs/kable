package com.juul.kable

import android.bluetooth.BluetoothGattDescriptor
import com.benasher44.uuid.Uuid

public actual data class Descriptor(
    internal val bluetoothGattDescriptor: BluetoothGattDescriptor,
) {

    public actual val uuid: Uuid by lazy { bluetoothGattDescriptor.uuid.toUuid() }
}
