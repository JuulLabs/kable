package com.juul.kable

@Deprecated(
    message = "Renamed to `CoreBluetoothPeripheral`.",
    replaceWith = ReplaceWith(
        expression = "CoreBluetoothPeripheral",
        imports = ["com.juul.kable.CoreBluetoothPeripheral"],
    ),
    level = DeprecationLevel.HIDDEN,
)
public typealias ApplePeripheral = CoreBluetoothPeripheral
