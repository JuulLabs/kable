package com.juul.kable

import kotlinx.io.IOException

public class L2CapException(
    message: String? = null,
    cause: Throwable? = null,
    public val code: Long,
) : IOException(message, cause)