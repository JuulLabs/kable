package com.juul.kable.server

import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_SECONDARY
import kotlin.uuid.toJavaUuid

internal fun ServiceBuilder.build() = BluetoothGattService(
    uuid.toJavaUuid(),
    if (primary) SERVICE_TYPE_PRIMARY else SERVICE_TYPE_SECONDARY,
).apply {
    this@build.characteristics.values.forEach { characteristic ->
        addCharacteristic(characteristic.build())
    }
}
