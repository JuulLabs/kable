package com.juul.kable

public actual class PeripheralBuilder internal actual constructor() {

    internal var onConnect: OnConnectAction = {}
    public actual fun onConnect(action: OnConnectAction) {
        onConnect = action
    }
}
