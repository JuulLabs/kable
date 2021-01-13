package com.juul.kable

/** Failure occurred with the underlying Bluetooth Low Energy system. */
public open class BluetoothLeException internal constructor(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

public expect open class IOException internal constructor(
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
