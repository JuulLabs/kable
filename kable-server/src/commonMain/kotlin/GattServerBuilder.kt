package com.juul.kable.server

import com.juul.kable.ExperimentalKableApi
import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder
import kotlin.uuid.Uuid

@ExperimentalKableApi
public class GattServerBuilder internal constructor() {

    internal var logging: Logging = Logging()
    private val services = mutableMapOf<Uuid, ServiceBuilder>()

    /** Configures [logging][Logging] of this [GattServer]. */
    public fun logging(init: LoggingBuilder) {
        logging = Logging().apply(init)
    }

    /**
     * Declares a GATT service identified by [uuid].
     *
     * @param uuid of the service, must be unique within this [GattServer].
     * @param primary whether the service is a primary (vs. secondary) service.
     */
    public fun service(
        uuid: Uuid,
        primary: Boolean = true,
        builderAction: ServiceBuilder.() -> Unit,
    ) {
        require(uuid !in services) { "Service $uuid already declared" }
        services[uuid] = ServiceBuilder(uuid, primary).apply(builderAction)
    }

    internal fun build(): ServerProfile =
        ServerProfile(services.values.map(ServiceBuilder::build))
}
