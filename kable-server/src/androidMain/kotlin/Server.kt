package com.juul.kable.server

import kotlinx.coroutines.CoroutineScope

public actual fun Server(builder: ServerBuilder.() -> Unit): Server =
    BluetoothGattServer(ServerBuilder(CoroutineScope()).apply(builder))
