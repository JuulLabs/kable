package com.juul.kable.server

public interface CharacteristicSink {
    public suspend fun send(value: ByteArray)
}
