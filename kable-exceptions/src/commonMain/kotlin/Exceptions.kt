package com.juul.kable

import kotlinx.io.IOException

/** Failure occurred with the underlying Bluetooth system. */
public open class BluetoothException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

public class LocationManagerUnavailableException(
    message: String? = null,
    cause: Throwable? = null,
) : BluetoothException(message, cause)

public open class NotConnectedException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)

public class ConnectionRejectedException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)

public class GattStatusException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)
