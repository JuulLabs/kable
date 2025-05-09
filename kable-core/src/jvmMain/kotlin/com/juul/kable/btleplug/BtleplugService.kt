package com.juul.kable.btleplug

import com.juul.kable.DiscoveredCharacteristic
import com.juul.kable.DiscoveredService
import kotlin.uuid.Uuid
import com.juul.kable.btleplug.ffi.Service as FfiService

internal data class BtleplugService(
    val service: FfiService,
) : DiscoveredService {
    override val serviceUuid: Uuid =
        Uuid.parse(service.uuid)

    override val characteristics: List<DiscoveredCharacteristic> =
        service.characteristics.map(::BtleplugCharacteristic)
}
