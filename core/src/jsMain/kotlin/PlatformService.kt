package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.external.BluetoothRemoteGATTService
import kotlinx.coroutines.await

internal data class PlatformService(
    override val serviceUuid: Uuid,
    val bluetoothRemoteGATTService: BluetoothRemoteGATTService,
    val characteristics: List<PlatformCharacteristic>,
) : Service

internal fun PlatformService.toDiscoveredService() = DiscoveredService(
    serviceUuid = serviceUuid,
    characteristics = characteristics.map { it.toDiscoveredCharacteristic() },
)

internal suspend fun BluetoothRemoteGATTService.toPlatformService(): PlatformService {
    val serviceUuid = uuid.toUuid()
    val characteristics = getCharacteristics()
        .await()
        .map { characteristic ->
            console.dir(characteristic)
            characteristic.toPlatformCharacteristic(serviceUuid)
        }

    return PlatformService(
        serviceUuid = serviceUuid,
        characteristics = characteristics,
        bluetoothRemoteGATTService = this,
    )
}
