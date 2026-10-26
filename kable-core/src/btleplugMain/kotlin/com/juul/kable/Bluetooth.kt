package com.juul.kable

@Deprecated(
    message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
        "Will be removed in a future release. " +
        "See https://github.com/JuulLabs/kable/issues/737 for more details.",
    level = DeprecationLevel.ERROR,
)
public actual enum class Reason {
    // Not implemented.
}
