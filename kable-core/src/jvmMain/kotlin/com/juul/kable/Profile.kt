package com.juul.kable

import kotlin.uuid.Uuid

internal actual class PlatformService
internal actual class PlatformCharacteristic
internal actual class PlatformDescriptor

/** Wrapper around platform specific Bluetooth LE service. Holds a strong reference to underlying service. */
public actual class DiscoveredService : Service {
    internal actual val service: PlatformService
        get() = jvmNotImplementedException()
    public actual val characteristics: List<DiscoveredCharacteristic>
        get() = jvmNotImplementedException()
    actual override val serviceUuid: Uuid
        get() = jvmNotImplementedException()
}

/** Wrapper around platform specific Bluetooth LE characteristic. Holds a strong reference to underlying characteristic. */
public actual class DiscoveredCharacteristic : Characteristic {
    internal actual val characteristic: PlatformCharacteristic
        get() = jvmNotImplementedException()
    public actual val descriptors: List<DiscoveredDescriptor>
        get() = jvmNotImplementedException()
    public actual val properties: Characteristic.Properties
        get() = jvmNotImplementedException()
    actual override val serviceUuid: Uuid
        get() = jvmNotImplementedException()
    actual override val characteristicUuid: Uuid
        get() = jvmNotImplementedException()
}

/** Wrapper around platform specific Bluetooth LE descriptor. Holds a strong reference to underlying descriptor. */
public actual class DiscoveredDescriptor : Descriptor {
    internal actual val descriptor: PlatformDescriptor
        get() = jvmNotImplementedException()
    actual override val serviceUuid: Uuid
        get() = jvmNotImplementedException()
    actual override val characteristicUuid: Uuid
        get() = jvmNotImplementedException()
    actual override val descriptorUuid: Uuid
        get() = jvmNotImplementedException()
}
