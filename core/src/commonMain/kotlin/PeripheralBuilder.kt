package com.juul.kable

internal typealias OnConnectAction = suspend PeripheralIo.() -> Unit

public expect class PeripheralBuilder internal constructor() {

    public fun onConnect(action: OnConnectAction)
}
