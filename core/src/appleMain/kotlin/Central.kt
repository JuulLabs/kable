package com.juul.kable

import com.benasher44.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

public fun CoroutineScope.central(): Central = Central(coroutineContext)

public actual class Central internal constructor(
    parentCoroutineContext: CoroutineContext
) {

    private val scope = CoroutineScope(parentCoroutineContext + Job(parentCoroutineContext[Job]))
    private val manager = scope.centralManager()

    public actual fun scanner(
        services: List<Uuid>?
    ): Scanner = Scanner(manager, services)

    public actual fun peripheral(
        advertisement: Advertisement
    ): Peripheral = scope.peripheral(manager, advertisement.cbPeripheral)
}
