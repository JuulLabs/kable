package com.juul.kable

public enum class UnmetRequirementReason {
    BluetoothDisabled,
}

public open class UnmetRequirementException(
    public val reason: UnmetRequirementReason,
    message: String,
    cause: Throwable? = null,
) : IOException(message, cause)
