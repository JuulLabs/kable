package com.juul.kable

import kotlinx.io.IOException

public class GattStatusException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)
