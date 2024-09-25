package com.juul.kable

import com.juul.kable.CentralManagerDelegate.ConnectionEvent
import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidConnect
import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidDisconnect
import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidFailToConnect

internal fun ConnectionEvent.toState(): State = when (this) {
    is DidConnect -> State.Connecting.Services
    is DidFailToConnect -> State.Disconnected(error?.toStatus())
    is DidDisconnect -> State.Disconnected(error?.toStatus())
}
