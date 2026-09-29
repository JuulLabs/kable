package com.juul.kable

import kotlinx.io.IOException

/** A failure while opening or communicating over an [L2CapSocket]. */
public class L2CapException(
    message: String? = null,
    cause: Throwable? = null,
    /**
     * Platform error code, if any: `BluetoothSocketException.errorCode` on Android (API 34+) or
     * `NSError.code` on Apple platforms.
     */
    public val code: Long? = null,
) : IOException(message, cause)
