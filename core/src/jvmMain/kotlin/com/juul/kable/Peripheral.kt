package com.juul.kable

import kotlinx.coroutines.CoroutineScope

public actual fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral {
    jvmNotImplementedException()
}
