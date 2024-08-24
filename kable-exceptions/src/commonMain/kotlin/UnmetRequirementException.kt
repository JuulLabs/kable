package com.juul.kable

public enum class UnmetRequirementReason {

    /**
     * Only applicable on Android 11 (API 30) and lower, where location services are required to
     * perform a scan.
     */
    LocationServicesDisabled,

    BluetoothDisabled,
}

public open class UnmetRequirementException(
    public val reason: UnmetRequirementReason,
    message: String,
    cause: Throwable? = null,
) : IOException(message, cause)
