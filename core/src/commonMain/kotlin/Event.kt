package com.juul.kable

public sealed class Event {

    /**
     * Triggered when a connection is established and ready. A connection is considered "ready" after service discovery
     * is complete and observations (if any) have been rewired.
     */
    public object Ready : Event()

    /** Triggered either after an established connection has dropped or after a connection attempt has failed. */
    public object Disconnected : Event()

    /** Triggered when a connection request is rejected by the system (e.g. bluetooth hardware unavailable). */
    public object Rejected : Event()
}

public suspend fun Event.onReady(
    action: suspend () -> Unit,
) {
    if (this === Event.Ready) action.invoke()
}

public suspend fun Event.onDisconnected(
    action: suspend () -> Unit,
) {
    if (this === Event.Disconnected) action.invoke()
}

public suspend fun Event.onRejected(
    action: suspend () -> Unit,
) {
    if (this === Event.Rejected) action.invoke()
}
