package com.juul.kable.bluetooth

import com.juul.kable.CentralManager
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import platform.CoreBluetooth.CBCentralManagerStateResetting
import platform.CoreBluetooth.CBCentralManagerStateUnknown
import platform.CoreBluetooth.CBCentralManagerStateUnsupported

internal actual suspend fun isSupported(): Boolean =
    CentralManager.Default.delegate
        .state
        .filterNot { it == CBCentralManagerStateResetting || it == CBCentralManagerStateUnknown }
        .first() != CBCentralManagerStateUnsupported
