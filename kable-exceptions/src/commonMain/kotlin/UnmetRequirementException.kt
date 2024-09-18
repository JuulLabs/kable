package com.juul.kable

import kotlinx.io.IOException

public enum class UnmetRequirementReason {

    /** Not applicable on JavaScript. */
    BluetoothDisabled,

    /**
     * Only applicable on Android 11 (API 30) and lower, where location services are required to
     * perform a scan.
     */
    LocationServicesDisabled,
}

public open class UnmetRequirementException(
    public val reason: UnmetRequirementReason,
    message: String,
    cause: Throwable? = null,
) : IOException(message, cause)
