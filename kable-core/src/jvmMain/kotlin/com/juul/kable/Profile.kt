package com.juul.kable

import kotlin.uuid.Uuid

internal actual class PlatformService
internal actual class PlatformCharacteristic
internal actual class PlatformDescriptor

internal actual class PlatformDiscoveredService : DiscoveredService {
    actual val service: PlatformService
        get() = jvmNotImplementedException()
    actual override val characteristics: List<PlatformDiscoveredCharacteristic>
        get() = jvmNotImplementedException()
    actual override val serviceUuid: Uuid
        get() = jvmNotImplementedException()
}

internal actual class PlatformDiscoveredCharacteristic : DiscoveredCharacteristic {
    actual val characteristic: PlatformCharacteristic
        get() = jvmNotImplementedException()
    actual override val descriptors: List<PlatformDiscoveredDescriptor>
        get() = jvmNotImplementedException()
    actual override val properties: Characteristic.Properties
        get() = jvmNotImplementedException()
    actual override val serviceUuid: Uuid
        get() = jvmNotImplementedException()
    actual override val characteristicUuid: Uuid
        get() = jvmNotImplementedException()
}

internal actual class PlatformDiscoveredDescriptor : DiscoveredDescriptor {
    actual val descriptor: PlatformDescriptor
        get() = jvmNotImplementedException()
    actual override val serviceUuid: Uuid
        get() = jvmNotImplementedException()
    actual override val characteristicUuid: Uuid
        get() = jvmNotImplementedException()
    actual override val descriptorUuid: Uuid
        get() = jvmNotImplementedException()
}
