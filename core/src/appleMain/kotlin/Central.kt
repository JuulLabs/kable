package com.juul.kable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

public fun CoroutineScope.central(): AppleCentral = AppleCentral(coroutineContext)

public class AppleCentral internal constructor(
    parentCoroutineContext: CoroutineContext
) : Central {

    private val scope = CoroutineScope(parentCoroutineContext + Job(parentCoroutineContext[Job]))
    private val manager = scope.centralManager()

    public override fun scanner(): Scanner = Scanner(manager, services)

    public override fun peripheral(
        advertisement: Advertisement
    ): Peripheral = scope.peripheral(manager, advertisement.cbPeripheral)
}
