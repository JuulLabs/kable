package com.juul.kable

/** https://developer.apple.com/documentation/corebluetooth/cbmanagerstate */
@Deprecated(
    message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
        "Will be removed in a future release. " +
        "See https://github.com/JuulLabs/kable/issues/737 for more details.",
    level = DeprecationLevel.ERROR,
)
public actual enum class Reason {
    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
        level = DeprecationLevel.ERROR,
    )
    Off, // CBManagerState.poweredOff

    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
        level = DeprecationLevel.ERROR,
    )
    Resetting, // CBManagerState.resetting

    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
        level = DeprecationLevel.ERROR,
    )
    Unauthorized, // CBManagerState.unauthorized

    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
        level = DeprecationLevel.ERROR,
    )
    Unsupported, // CBManagerState.unsupported

    @Deprecated(
        message = "`Bluetooth.availability` has inconsistent behavior across platforms. " +
            "Will be removed in a future release. " +
            "See https://github.com/JuulLabs/kable/issues/737 for more details.",
        level = DeprecationLevel.ERROR,
    )
    Unknown, // CBManagerState.unknown
}
