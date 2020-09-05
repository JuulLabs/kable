package com.juul.kable

public sealed class State {

    public object Connecting : State()

    public object Connected : State()

    public object Disconnecting : State()

    public data class Disconnected internal constructor(
        val cause: Error?,
    ) : State()

    public data class Cancelled internal constructor(
        val cause: Error?,
    ) : State()
}
