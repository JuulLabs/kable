package com.juul.kable.server

import kotlinx.coroutines.CoroutineScope

internal class CBPeripheralManagerCentralBuilder(
    override val centralScope: CoroutineScope,
) : ServerBuilder {
}
