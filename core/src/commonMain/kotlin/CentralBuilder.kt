package com.juul.kable

public expect class CentralBuilder internal constructor() {
    public var stateRestoration: Boolean
    internal fun build(): CentralManager
}
