package com.juul.kable

internal typealias OnConnectAction = suspend Peripheral.() -> Unit

public expect class PeripheralBuilder internal constructor() {

    public fun onConnect(action: OnConnectAction)
}
