package com.juul.kable

import android.content.Context
import com.benasher44.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

public fun CoroutineScope.central(
    context: Context
): Central = Central(coroutineContext, context)

public actual class Central internal constructor(
    parentCoroutineContext: CoroutineContext,
    androidContext: Context
) {

    private val scope = CoroutineScope(parentCoroutineContext + Job(parentCoroutineContext[Job]))
    internal val applicationContext = androidContext.applicationContext

    public actual fun scanner(services: List<Uuid>?): Scanner = Scanner()
    
    public actual fun peripheral(
        advertisement: Advertisement
    ): Peripheral = scope.peripheral(applicationContext, advertisement.scanResult.device)
}
