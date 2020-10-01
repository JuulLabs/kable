package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.BluetoothRemoteGATTCharacteristic
import com.juul.kable.external.BluetoothRemoteGATTService

public actual class Service internal constructor(
    private val bluetoothRemoteGATTService: BluetoothRemoteGATTService,
    bluetoothRemoteGATTCharacteristics: Array<BluetoothRemoteGATTCharacteristic>,
) {

    public actual val uuid: Uuid by lazy { bluetoothRemoteGATTService.uuid.toUuid() }

    public actual val characteristics: List<Characteristic> =
        bluetoothRemoteGATTCharacteristics.map { Characteristic(serviceUuid = uuid, it) }
}
