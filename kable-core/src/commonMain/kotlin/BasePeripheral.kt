package com.juul.kable

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope

internal abstract class BasePeripheral(identifier: Identifier) : Peripheral {

    override val scope = CoroutineScope(
        SilentSupervisor() + CoroutineName("Kable/Peripheral/$identifier"),
    )
}
