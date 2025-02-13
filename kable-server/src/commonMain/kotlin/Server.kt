package com.juul.kable.server

import kotlinx.coroutines.flow.StateFlow

public class Client

/*
 * Scanner    (Central)    => Client
 * Advertiser (Peripheral) => Server
 */

public interface Server {
    public val clients: StateFlow<List<Client>>
    public fun start()
    public suspend fun stop()
}

public expect fun Server(builder: ServerBuilder.() -> Unit): Server
