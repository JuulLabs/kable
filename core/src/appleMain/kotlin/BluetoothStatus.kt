package com.juul.kable

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import platform.CoreBluetooth.CBCentralManagerStatePoweredOff
import platform.CoreBluetooth.CBCentralManagerStatePoweredOn
import platform.CoreBluetooth.CBCentralManagerStateResetting
import platform.CoreBluetooth.CBCentralManagerStateUnauthorized
import platform.CoreBluetooth.CBCentralManagerStateUnknown
import platform.CoreBluetooth.CBCentralManagerStateUnsupported

public actual val bluetoothStatusFlow: Flow<BluetoothState>
    get() {
        return CentralManager.Default.delegate.state.map {
            when (it) {
                CBCentralManagerStatePoweredOn -> BluetoothState.poweredOn
                CBCentralManagerStateUnknown -> BluetoothState.unknown
                CBCentralManagerStatePoweredOff -> BluetoothState.poweredOff
                CBCentralManagerStateResetting -> BluetoothState.resetting
                CBCentralManagerStateUnsupported -> BluetoothState.unsupported
                CBCentralManagerStateUnauthorized -> BluetoothState.unauthorized
                else -> { BluetoothState.unknown }
            }
        }
    }
