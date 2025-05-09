package com.juul.kable.btleplug

import com.juul.kable.Characteristic
import com.juul.kable.DiscoveredCharacteristic
import com.juul.kable.DiscoveredDescriptor
import kotlin.uuid.Uuid
import com.juul.kable.btleplug.ffi.Characteristic as FfiCharacteristic

internal data class BtleplugCharacteristic(
    val characteristic: FfiCharacteristic,
) : DiscoveredCharacteristic {
    override val serviceUuid: Uuid =
        Uuid.parse(characteristic.service)

    override val characteristicUuid: Uuid =
        Uuid.parse(characteristic.uuid)

    override val descriptors: List<DiscoveredDescriptor> =
        characteristic.descriptors.map(::BtleplugDescriptor)

    override val properties: Characteristic.Properties =
        Characteristic.Properties(characteristic.properties.bits.toInt())
}
