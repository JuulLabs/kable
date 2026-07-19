package com.juul.kable

import kotlinx.io.IOException

/** Represents a failure while opening or communicating over an [L2CapSocket]. */
public class L2CapException(
    message: String? = null,
    cause: Throwable? = null,
    /**
     * Platform error code associated with the failure: `BluetoothSocketException.errorCode` on Android
     * (API 34+), `NSError.code` on Apple platforms, or `0` when no code is available.
     */
    public val code: Long,
) : IOException(message, cause)
