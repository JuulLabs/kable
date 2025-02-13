package com.juul.kable.server

import com.juul.kable.Bluetooth
import kotlin.uuid.Uuid

public class ServiceBuilder(
    parent: ServerBuilder,
    internal val uuid: Uuid,
    internal val primary: Boolean,
) {

    internal val scope = parent.scope.createChildScope()
    internal var characteristics = mutableMapOf<Uuid, CharacteristicBuilder>()

    public fun characteristic(uuid: Uuid, broadcast: Boolean = false, builder: CharacteristicBuilder.() -> Unit) {
        require(uuid !in characteristics) { "Characteristic $uuid already configured" }
        characteristics[uuid] = CharacteristicBuilder(this, uuid).apply(builder)
    }
}

public fun ServiceBuilder.characteristic(
    uuid: Int,
    broadcast: Boolean = false,
    builder: CharacteristicBuilder.() -> Unit,
): Unit = characteristic(Bluetooth.BaseUuid + uuid, broadcast, builder)
