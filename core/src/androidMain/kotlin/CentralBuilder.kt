package com.juul.kable

public actual class CentralBuilder internal actual constructor() {

    public actual var stateRestoration: Boolean = false

    internal actual fun build(): CentralManager {
        throw NotImplementedError("CentralManager is no-op on Android.")
    }
}
