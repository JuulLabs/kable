package com.juul.kable

import android.bluetooth.BluetoothAdapter

public actual typealias Identifier = String

public actual fun String.toIdentifier(): Identifier {
    require(BluetoothAdapter.checkBluetoothAddress(uppercase())) {
        "MAC Address has invalid format: $this"
    }
    return this
}
