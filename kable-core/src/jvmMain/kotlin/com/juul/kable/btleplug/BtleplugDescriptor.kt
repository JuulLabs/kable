package com.juul.kable.btleplug

import com.juul.kable.DiscoveredDescriptor
import kotlin.uuid.Uuid
import com.juul.kable.btleplug.ffi.Descriptor as FfiDescriptor

internal data class BtleplugDescriptor(
    val descriptor: FfiDescriptor,
) : DiscoveredDescriptor {
    override val serviceUuid: Uuid =
        Uuid.parse(descriptor.service)

    override val characteristicUuid: Uuid =
        Uuid.parse(descriptor.characteristic)

    override val descriptorUuid: Uuid =
        Uuid.parse(descriptor.uuid)
}
