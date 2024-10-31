package com.juul.kable.bluetooth

import com.juul.kable.CentralManager
import com.juul.kable.InternalException
import com.juul.kable.UnmetRequirementException
import com.juul.kable.UnmetRequirementReason.BluetoothDisabled
import platform.CoreBluetooth.CBManagerState
import platform.CoreBluetooth.CBManagerStatePoweredOff
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBManagerStateResetting
import platform.CoreBluetooth.CBManagerStateUnauthorized
import platform.CoreBluetooth.CBManagerStateUnknown
import platform.CoreBluetooth.CBManagerStateUnsupported

/**
 * @throws UnmetRequirementException If [CentralManager] state is not [CBManagerStatePoweredOn].
 */
internal fun CentralManager.checkBluetoothIsOn() {
    val actual = delegate.state.value
    val expected = CBManagerStatePoweredOn
    if (actual != expected) {
        throw UnmetRequirementException(
            reason = BluetoothDisabled,
            message = "Bluetooth was ${nameFor(actual)}, but ${nameFor(expected)} was required",
        )
    }
}

private fun nameFor(state: CBManagerState) = when (state) {
    CBManagerStatePoweredOff -> "PoweredOff"
    CBManagerStatePoweredOn -> "PoweredOn"
    CBManagerStateResetting -> "Resetting"
    CBManagerStateUnauthorized -> "Unauthorized"
    CBManagerStateUnknown -> "Unknown"
    CBManagerStateUnsupported -> "Unsupported"
    else -> throw InternalException("Unsupported bluetooth state: $state")
}
