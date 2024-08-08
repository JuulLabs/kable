package com.juul.kable

@Deprecated(
    message = "Renamed to `WebBluetoothPeripheral`.",
    replaceWith = ReplaceWith(
        expression = "WebBluetoothPeripheral",
        imports = ["com.juul.kable.WebBluetoothPeripheral"],
    ),
    level = DeprecationLevel.ERROR,
)
public typealias JsPeripheral = WebBluetoothPeripheral
