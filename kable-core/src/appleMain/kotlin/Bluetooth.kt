package com.juul.kable

import com.juul.kable.AvailabilityReason.Off
import com.juul.kable.AvailabilityReason.Resetting
import com.juul.kable.AvailabilityReason.Unauthorized
import com.juul.kable.AvailabilityReason.Unknown
import com.juul.kable.AvailabilityReason.Unsupported
import com.juul.kable.Bluetooth.Availability.Available
import com.juul.kable.Bluetooth.Availability.Unavailable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import platform.CoreBluetooth.CBCentralManagerStatePoweredOff
import platform.CoreBluetooth.CBCentralManagerStatePoweredOn
import platform.CoreBluetooth.CBCentralManagerStateResetting
import platform.CoreBluetooth.CBCentralManagerStateUnauthorized
import platform.CoreBluetooth.CBCentralManagerStateUnsupported

/** https://developer.apple.com/documentation/corebluetooth/cbmanagerstate */
public actual enum class AvailabilityReason {
    Off, // CBManagerState.poweredOff
    Resetting, // CBManagerState.resetting
    Unauthorized, // CBManagerState.unauthorized
    Unsupported, // CBManagerState.unsupported
    Unknown, // CBManagerState.unknown
}

internal actual val bluetoothAvailability: Flow<Bluetooth.Availability> = flow {
    // flow + emitAll dance so that lazy `CentralManager.Default` is not initialized until this flow is active.
    emitAll(CentralManager.Default.delegate.state)
}.map { state ->
    when (state) {
        CBCentralManagerStatePoweredOn -> Available
        CBCentralManagerStatePoweredOff -> Unavailable(reason = Off)
        CBCentralManagerStateResetting -> Unavailable(reason = Resetting)
        CBCentralManagerStateUnauthorized -> Unavailable(reason = Unauthorized)
        CBCentralManagerStateUnsupported -> Unavailable(reason = Unsupported)
        else -> Unavailable(reason = Unknown)
    }
}
