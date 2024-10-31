package com.juul.kable

import kotlinx.io.IOException

public open class NotConnectedException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)
