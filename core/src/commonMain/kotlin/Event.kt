package com.juul.kable

public sealed class Event {

    /** Triggered upon a connection being successfully established. */
    public data class Connected(
        val peripheral: Peripheral,
    ) : Event()

    /**
     * Triggered either immediately after an established connection has dropped or after a failed
     * connection attempt.
     *
     * @param wasConnected is `true` if event follows an established connection, or `false` if previous connection attempt failed.
     */
    public data class Disconnected(
        val wasConnected: Boolean,
    ) : Event()

    /**
     * Triggered when the connection request was rejected by the system (e.g. bluetooth hardware
     * unavailable).
     */
    public data class Rejected(
        val cause: Throwable,
    ) : Event()
}

public suspend fun Event.onConnected(
    action: suspend Peripheral.() -> Unit,
) {
    if (this is Event.Connected) action.invoke(peripheral)
}

public suspend fun Event.onDisconnected(
    action: suspend Event.Disconnected.() -> Unit,
) {
    if (this is Event.Disconnected) action.invoke(this)
}

public suspend fun Event.onRejected(
    action: suspend Event.Rejected.() -> Unit,
) {
    if (this is Event.Rejected) action.invoke(this)
}
