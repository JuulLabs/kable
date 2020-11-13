package com.juul.kable

public expect open class IOException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception

public class ConnectionRejectedException internal constructor(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)

public class NotReadyException internal constructor(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)

public class GattStatusException internal constructor(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)

public class ConnectionLostException internal constructor(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)
