package com.juul.kable.server

import com.juul.kable.ExperimentalKableApi
import com.juul.kable.server.logs.Logger

@ExperimentalKableApi
@Suppress("FunctionName") // Builder function.
public actual fun GattServer(builderAction: GattServerBuilder.() -> Unit): GattServer {
    val builder = GattServerBuilder().apply(builderAction)
    return GattServerImpl(
        profile = builder.build(),
        logging = builder.logging,
        engine = AppleServerEngine(Logger(builder.logging)),
    )
}
