package com.juul.kable

import kotlinx.coroutines.CoroutineName

internal abstract class BasePeripheral(identifier: Identifier) : Peripheral {

    override val coroutineContext =
        SilentSupervisor() + CoroutineName("Kable/Peripheral/$identifier")
}
