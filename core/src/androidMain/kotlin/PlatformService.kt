package com.juul.kable

import android.bluetooth.BluetoothGattService
import android.util.Log
import com.benasher44.uuid.Uuid

internal data class PlatformService(
    override val serviceUuid: Uuid,
    val bluetoothGattService: BluetoothGattService,
    val characteristics: List<PlatformCharacteristic>,
) : Service

internal fun PlatformService.toDiscoveredService() = DiscoveredService(
    serviceUuid = serviceUuid,
    characteristics = characteristics.map { it.toDiscoveredCharacteristic() },
)

internal fun BluetoothGattService.toPlatformService(): PlatformService {
    val serviceUuid = uuid.toUuid()
    val characteristics = characteristics
        .map { characteristic ->
            Log.d(TAG, characteristic.toString())
            characteristic.toPlatformCharacteristic()
        }

    return PlatformService(
        serviceUuid = serviceUuid,
        characteristics = characteristics,
        bluetoothGattService = this,
    )
}
