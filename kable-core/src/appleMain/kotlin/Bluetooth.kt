package com.juul.kable

import com.juul.kable.Bluetooth.Availability.Available
import com.juul.kable.Bluetooth.Availability.Unavailable
import com.juul.kable.Reason.Off
import com.juul.kable.Reason.Resetting
import com.juul.kable.Reason.Unauthorized
import com.juul.kable.Reason.Unknown
import com.juul.kable.Reason.Unsupported
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
@Deprecated(
    message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
        "Will be removed in a future release. " +
        "See https://github.com/JuulLabs/kable/issues/737 for more details.",
)
public actual enum class Reason {
    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
    )
    Off, // CBManagerState.poweredOff

    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
    )
    Resetting, // CBManagerState.resetting

    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
    )
    Unauthorized, // CBManagerState.unauthorized

    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
    )
    Unsupported, // CBManagerState.unsupported

    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
    )
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
