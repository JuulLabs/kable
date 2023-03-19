package com.juul.kable

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom

public actual typealias Identifier = Uuid

public actual val Peripheral.identifier: Identifier
    get() = (this as CBPeripheralCoreBluetoothPeripheral).platformIdentifier.toUuid()

public actual fun String.toIdentifier(): Identifier = uuidFrom(this)
