package com.juul.kable

/** Failure occurred with the underlying Bluetooth system. */
public open class BluetoothException internal constructor(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

@Deprecated(
    message = "Class has been renamed.",
    ReplaceWith("BluetoothException", "com.juul.kable.BluetoothException"),
    level = DeprecationLevel.ERROR,
)
public typealias BluetoothLeException = BluetoothException

public class BluetoothDisabledException internal constructor(
    message: String? = null,
    cause: Throwable? = null,
) : BluetoothException(message, cause)

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
