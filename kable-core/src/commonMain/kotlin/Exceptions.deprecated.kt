package com.juul.kable

@Deprecated(
    message = "BluetoothException was removed. Closest replacement is UnmetRequirementException.",
    replaceWith = ReplaceWith("UnmetRequirementException"),
    level = DeprecationLevel.ERROR,
)
public typealias BluetoothException = UnmetRequirementException

@Deprecated(
    message = "LocationManagerUnavailableException replaced by UnmetRequirementException w/ a `reason` of `LocationServicesDisabled`.",
    replaceWith = ReplaceWith("UnmetRequirementException"),
    level = DeprecationLevel.ERROR,
)
public typealias LocationManagerUnavailableException = UnmetRequirementException

@Deprecated(
    message = "BluetoothDisabledException replaced by UnmetRequirementException w/ a `reason` of `BluetoothDisabled`.",
    replaceWith = ReplaceWith("UnmetRequirementException"),
    level = DeprecationLevel.ERROR,
)
public typealias BluetoothDisabledException = UnmetRequirementException

@Deprecated(
    message = "All connection loss exceptions are now represented as NotConnectedException.",
    replaceWith = ReplaceWith("NotConnectedException"),
    level = DeprecationLevel.ERROR,
)
public typealias ConnectionLostException = NotConnectedException

@Deprecated(
    message = "Kable now uses kotlinx-io's IOException.",
    replaceWith = ReplaceWith(
        "IOException",
        imports = ["kotlinx.io.IOException"],
    ),
    level = DeprecationLevel.ERROR,
)
public typealias IOException = kotlinx.io.IOException

@Deprecated(
    message = "All connection loss exceptions are now represented as NotConnectedException.",
    replaceWith = ReplaceWith("NotConnectedException"),
    level = DeprecationLevel.ERROR,
)
public typealias NotReadyException = NotConnectedException
