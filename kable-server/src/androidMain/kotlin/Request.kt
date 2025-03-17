package com.juul.kable.server

internal sealed class Request {

    abstract val requestId: Int

    data class CharacteristicReadRequest(
        override val requestId: Int,
    ) : Request()

    data class CharacteristicWriteRequest(
        override val requestId: Int,
        characteristic,
        preparedWrite,
        responseNeeded,
        val value: ByteArray?,
    ) : Request()
}
