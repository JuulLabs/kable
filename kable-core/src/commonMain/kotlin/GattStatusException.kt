package com.juul.kable

import kotlinx.io.IOException

/** Represents an Android GATT status failure. */
public class GattStatusException(
    message: String? = null,
    cause: Throwable? = null,
    public val status: Int,
) : IOException(message, cause)
