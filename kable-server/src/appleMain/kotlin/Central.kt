package com.juul.kable.server

import kotlinx.coroutines.CoroutineScope

public fun Server(builder: ServerBuilder.() -> Unit): Server =
    CBPeripheralManagerCentralBuilder(CoroutineScope()).apply(builder).build()
