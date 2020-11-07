package com.juul.kable

import android.bluetooth.BluetoothGattService
import com.benasher44.uuid.Uuid

@Suppress("PROPERTY_TYPE_MISMATCH_ON_OVERRIDE") // https://youtrack.jetbrains.com/issue/KTIJ-405
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
    val serviceUuid = uuid
    val characteristics = characteristics
        .map { characteristic -> characteristic.toPlatformCharacteristic() }

    return PlatformService(
        serviceUuid = serviceUuid,
        characteristics = characteristics,
        bluetoothGattService = this,
    )
}
