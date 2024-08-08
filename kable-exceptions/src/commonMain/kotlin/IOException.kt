package com.juul.kable

public expect open class IOException(
    message: String?,
    cause: Throwable?,
) : Exception {
    public constructor()
    public constructor(message: String?)
    public constructor(cause: Throwable?)
}
