package com.juul.kable

@Deprecated(
    message = "BluetoothDisabledException replaced by UnmetRequirementException w/ a `reason` of `BluetoothDisabled`.",
    replaceWith = ReplaceWith("UnmetRequirementException"),
)
public typealias BluetoothDisabledException = UnmetRequirementException

@Deprecated(
    message = "All connection loss exceptions are now represented as NotConnectedException.",
    replaceWith = ReplaceWith("NotConnectedException"),
)
public typealias ConnectionLostException = NotConnectedException

@Deprecated(
    message = "Kable now uses kotlinx-io's IOException.",
    replaceWith = ReplaceWith(
        "IOException",
        imports = ["kotlinx.io.IOException"],
    ),
)
public typealias IOException = kotlinx.io.IOException

@Deprecated(
    message = "All connection loss exceptions are now represented as NotConnectedException.",
    replaceWith = ReplaceWith("NotConnectedException"),
)
public typealias NotReadyException = NotConnectedException
