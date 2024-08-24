package com.juul.kable.scan.requirements

import android.bluetooth.le.BluetoothLeScanner
import com.juul.kable.UnmetRequirementException
import com.juul.kable.UnmetRequirementReason.BluetoothDisabled
import com.juul.kable.getBluetoothAdapter

/**
 * @throws IllegalStateException If bluetooth is not supported.
 * @throws UnmetRequirementException If bluetooth is disabled.
 */
internal fun requireBluetoothLeScanner(): BluetoothLeScanner =
    getBluetoothAdapter().bluetoothLeScanner
        ?: throw UnmetRequirementException(BluetoothDisabled, "Bluetooth disabled")
