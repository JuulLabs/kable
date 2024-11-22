package com.juul.kable

import com.juul.kable.logs.Logging
import kotlinx.coroutines.CoroutineName

internal abstract class BasePeripheral(logging: Logging, identifier: Identifier) : Peripheral {

    override val coroutineContext =
        LoggingSupervisor(logging, identifier) + CoroutineName("Kable/Peripheral/$identifier")
}
