package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom

public fun characteristicOf(
    service: String,
    characteristic: String,
): Characteristic = LazyCharacteristic(
    serviceUuid = uuidFrom(service),
    characteristicUuid = uuidFrom(characteristic)
)

public interface Characteristic {
    public val serviceUuid: Uuid
    public val characteristicUuid: Uuid
}

public data class LazyCharacteristic internal constructor(
    override val serviceUuid: Uuid,
    override val characteristicUuid: Uuid,
) : Characteristic

public data class DiscoveredCharacteristic internal constructor(
    override val serviceUuid: Uuid,
    override val characteristicUuid: Uuid,
    public val descriptors: List<Descriptor>,
) : Characteristic

internal fun <T : Characteristic> List<T>.first(
    characteristicUuid: Uuid
): T = firstOrNull { it.characteristicUuid == characteristicUuid }
    ?: throw NoSuchElementException("Characteristic $characteristicUuid not found")
