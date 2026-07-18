package com.juul.kable.server

import com.juul.kable.ExperimentalKableApi

/**
 * Thrown from a [ReadAction] or [WriteAction] to reject the request with the specified ATT [error]
 * (communicated to the remote [Central] as an ATT Error Response).
 */
@ExperimentalKableApi
public class GattErrorException(
    public val error: AttError,
    message: String? = null,
) : Exception(message ?: "GATT error: $error")

/** Thrown when [advertising][GattServer.advertise] could not be started. */
@ExperimentalKableApi
public open class AdvertiseException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
