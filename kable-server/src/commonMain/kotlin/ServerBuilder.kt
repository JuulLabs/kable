package com.juul.kable.server

import com.juul.kable.Bluetooth
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope

public class ServerBuilder(
    internal val scope: CoroutineScope,
) {

    internal val services = mutableMapOf<Uuid, ServiceBuilder>()

    public fun service(
        uuid: Uuid,
        primary: Boolean = true,
        builder: ServiceBuilder.() -> Unit,
    ) {
        require(uuid !in services) { "Service $uuid already configured" }
        services[uuid] = ServiceBuilder(this, uuid, primary).apply(builder)
    }
}

public fun ServerBuilder.service(
    uuid: Int,
    primary: Boolean = true,
    builder: ServiceBuilder.() -> Unit,
): Unit = service(Bluetooth.BaseUuid + uuid, primary, builder)
