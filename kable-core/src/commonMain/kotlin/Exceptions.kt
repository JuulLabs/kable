package com.juul.kable

import kotlinx.io.IOException

public class ConnectionRejectedException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)

public class GattStatusException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)
