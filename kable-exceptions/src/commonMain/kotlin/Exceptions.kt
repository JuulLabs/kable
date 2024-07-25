package com.juul.kable

/** Failure occurred with the underlying Bluetooth system. */
public open class BluetoothException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

public class LocationManagerUnavailableException(
    message: String? = null,
    cause: Throwable? = null,
) : BluetoothException(message, cause)

public class BluetoothDisabledException(
    message: String? = null,
    cause: Throwable? = null,
) : BluetoothException(message, cause)

public expect open class IOException(
    message: String?,
    cause: Throwable?,
) : Exception {
    public constructor()
    public constructor(message: String?)
    public constructor(cause: Throwable?)
}

@Deprecated(
    message = "Renamed to InvalidStateException.",
    replaceWith = ReplaceWith("InvalidStateException"),
)
public typealias NotConnectedException = InvalidStateException

public open class InvalidStateException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)

public class ConnectionRejectedException(
    message: String? = null,
    cause: Throwable? = null,
) : InvalidStateException(message, cause)

@Deprecated(
    message = "Collapsed into InvalidStateException.",
    replaceWith = ReplaceWith("InvalidStateException"),
)
public typealias NotReadyException = InvalidStateException

@Deprecated(
    message = "Collapsed into InvalidStateException.",
    replaceWith = ReplaceWith("InvalidStateException"),
)
public typealias ConnectionLostException = InvalidStateException

public class GattStatusException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)
