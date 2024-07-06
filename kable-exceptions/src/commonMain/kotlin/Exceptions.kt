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

public open class NotConnectedException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)

public class ConnectionRejectedException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)

public class NotReadyException(
    message: String? = null,
    cause: Throwable? = null,
) : NotConnectedException(message, cause)

public class GattStatusException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)

public class ConnectionLostException(
    message: String? = null,
    cause: Throwable? = null,
) : NotConnectedException(message, cause)
