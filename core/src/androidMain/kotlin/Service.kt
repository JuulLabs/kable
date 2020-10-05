package com.juul.kable

import android.bluetooth.BluetoothGattService
import com.benasher44.uuid.Uuid

public actual class Service(
    private val bluetoothGattService: BluetoothGattService
) {

    public actual val uuid: Uuid
        get() = bluetoothGattService.uuid.toUuid()

    public actual val characteristics: List<Characteristic>
        get() = TODO("Not yet implemented")
}
