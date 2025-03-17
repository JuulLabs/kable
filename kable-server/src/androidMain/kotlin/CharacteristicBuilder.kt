package com.juul.kable.server

import android.bluetooth.BluetoothGattCharacteristic
import kotlin.uuid.toJavaUuid

internal fun CharacteristicBuilder.build(): BluetoothGattCharacteristic {
    val (properties, permissions) = properties.build()
    return BluetoothGattCharacteristic(
        uuid.toJavaUuid(),
        properties,
        permissions,
    )
}
