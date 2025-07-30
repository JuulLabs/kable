package com.juul.kable

@Deprecated(
    message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
        "Will be removed in a future release. " +
        "See https://github.com/JuulLabs/kable/issues/737 for more details.",
)
public actual enum class Reason {
    /** `window.navigator.bluetooth` is undefined. */
    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
    )
    BluetoothUndefined,
}

private const val AVAILABILITY_CHANGED = "availabilitychanged"
