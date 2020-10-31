package com.juul.kable

public sealed class State {
    public object Connecting : State()
    public object Connected : State()
    public object Disconnecting : State()
    public object Disconnected : State()
}
