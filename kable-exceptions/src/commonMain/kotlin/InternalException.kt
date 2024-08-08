package com.juul.kable

public class InternalException(
    message: String,
    cause: Throwable? = null,
) : IllegalStateException("$message, please report issue to https://github.com/JuulLabs/kable/issues and provide logs", cause)
