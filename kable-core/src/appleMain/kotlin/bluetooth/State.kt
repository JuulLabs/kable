package com.juul.kable.bluetooth

import com.juul.kable.Bluetooth.State
import com.juul.kable.Bluetooth.State.Unavailable.Reason.Unauthorized
import com.juul.kable.Bluetooth.State.Unknown.Reason.Resetting
import com.juul.kable.CentralManager
import com.juul.kable.InternalException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import platform.CoreBluetooth.CBCentralManagerStatePoweredOff
import platform.CoreBluetooth.CBCentralManagerStatePoweredOn
import platform.CoreBluetooth.CBCentralManagerStateResetting
import platform.CoreBluetooth.CBCentralManagerStateUnauthorized
import platform.CoreBluetooth.CBCentralManagerStateUnknown
import platform.CoreBluetooth.CBCentralManagerStateUnsupported

internal actual val state: Flow<State> = flow {
    // flow + emitAll dance so that lazy `CentralManager.Default` is not initialized until this flow is active.
    emitAll(CentralManager.Default.delegate.state)
}.map { state ->
    // https://developer.apple.com/documentation/corebluetooth/cbmanagerstate
    when (state) {
        CBCentralManagerStatePoweredOff -> State.Off
        CBCentralManagerStatePoweredOn -> State.On
        CBCentralManagerStateResetting -> State.Unknown(Resetting)
        CBCentralManagerStateUnauthorized -> State.Unavailable(Unauthorized)
        CBCentralManagerStateUnknown -> State.Unknown(null)
        CBCentralManagerStateUnsupported -> error("Bluetooth unsupported")
        else -> throw InternalException("Unknown state: $state")
    }
}
