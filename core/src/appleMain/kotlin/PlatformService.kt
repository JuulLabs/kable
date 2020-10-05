package com.juul.kable

import com.benasher44.uuid.Uuid
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBService
import platform.Foundation.NSLog

internal data class PlatformService(
    override val serviceUuid: Uuid,
    val cbService: CBService,
    val characteristics: List<PlatformCharacteristic>,
) : Service

internal fun PlatformService.toDiscoveredService() = DiscoveredService(
    serviceUuid = serviceUuid,
    characteristics = characteristics.map { it.toDiscoveredCharacteristic() },
)

internal fun CBService.toPlatformService(): PlatformService {
    val serviceUuid = UUID.toUuid()
    val platformCharacteristics = characteristics?.map { characteristic ->
        NSLog("%@", characteristic)
        characteristic as CBCharacteristic
        characteristic.toPlatformCharacteristic(serviceUuid)
    } ?: emptyList()

    return PlatformService(
        serviceUuid = serviceUuid,
        characteristics = platformCharacteristics,
        cbService = this,
    )
}
