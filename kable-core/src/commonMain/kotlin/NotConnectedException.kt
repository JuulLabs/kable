package com.juul.kable

import kotlinx.io.IOException

public open class NotConnectedException(
    message: String? = null,
    cause: Throwable? = null,

    /**
     * The status (cause) of the disconnect that triggered this exception, or `null` if
     * unavailable (e.g. disconnect was requested, or underlying platform did not provide a
     * status).
     */
    public val status: State.Disconnected.Status? = null,
) : IOException(message, cause)
